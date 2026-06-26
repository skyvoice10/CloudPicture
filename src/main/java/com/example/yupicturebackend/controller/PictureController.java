package com.example.yupicturebackend.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yupicturebackend.annotation.AuthCheck;
import com.example.yupicturebackend.api.aliyunai.AliYunAiApi;
import com.example.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.example.yupicturebackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.example.yupicturebackend.api.imageSearch.ImageSearchApiFacade;
import com.example.yupicturebackend.api.imageSearch.model.ImageSearchResult;
import com.example.yupicturebackend.common.BaseResponse;
import com.example.yupicturebackend.common.DeleteRequest;
import com.example.yupicturebackend.common.ResultUtils;
import com.example.yupicturebackend.constant.UserConstant;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.exception.ThrowUtils;
import com.example.yupicturebackend.manager.auth.SpaceUserAuthManager;
import com.example.yupicturebackend.manager.auth.StpKit;
import com.example.yupicturebackend.manager.auth.annotation.SaSpaceCheckPermission;
import com.example.yupicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.example.yupicturebackend.model.dto.picture.*;
import com.example.yupicturebackend.model.entity.Picture;
import com.example.yupicturebackend.model.entity.Space;
import com.example.yupicturebackend.model.entity.User;
import com.example.yupicturebackend.model.enums.PictureReviewStatusEnum;
import com.example.yupicturebackend.model.vo.PictureTagCategory;
import com.example.yupicturebackend.model.vo.PictureVo;
import com.example.yupicturebackend.service.PictureService;
import com.example.yupicturebackend.service.SpaceService;
import com.example.yupicturebackend.service.UserService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.swagger.annotations.Authorization;
import io.swagger.models.auth.In;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.protocol.HTTP;
import org.bouncycastle.pqc.jcajce.provider.qtesla.SignatureSpi;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {
    @Resource
    PictureService pictureService;
    @Resource
    UserService userService;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    SpaceService spaceService;
    @Resource
    AliYunAiApi aliYunAiApi;
    @Resource
    SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 本地缓存
     */
    private final Cache<String, String> LOCAL_CACHE= Caffeine.newBuilder()
            .initialCapacity(1024)
            .maximumSize(10_000L)   //  最大1000条
            //  缓存5分钟后移除
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    /**
     * 上传图片可重新上传
     */
    @PostMapping("/upload")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVo> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request
    ) {
        User loginUser = userService.getLoginUser(request);
        PictureVo pictureVo = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVo);
    }

    /**
     * 通过url上传图片
     */
    @PostMapping("/upload/url")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVo> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVo pictureVo = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVo);
    }

    /**
     * 更新图片(仅管理员)
     *
     * @param pictureUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        //  将dto转变成实体类
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        //  将list转化为string
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        //  数据校验
        pictureService.validatePicture(picture);
        Long id = pictureUpdateRequest.getId();
        //  判断是否存在
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //  填充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);
        //  操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取图片(管理员)
     *
     * @param id
     * @return
     */
    @PostMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(picture);
    }

    ;

    /**
     * 根据id获取图片(用户)
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVo> getPictureVoById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        //  空间权限校验
        Long spaceId=picture.getSpaceId();
        Space space=null;
        if(spaceId!=null){
            // 空间id不为空才开启sa-token权限校验，使未登录的用户也能查看公共图库
            // 编程式权限校验只对需要鉴权的代码块进行鉴权，比注解式更加灵活
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission,ErrorCode.NO_AUTH_ERROR);
            space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space==null,ErrorCode.NOT_FOUND_ERROR,"空间不存在");
            // 已改为使用注解鉴权
              // User loginUser=userService.getLoginUser(request);
              // pictureService.checkPictureAuth(loginUser,picture);
        }
        //  获取权限列表
        User loinUser=userService.getLoginUser(request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loinUser);
        PictureVo pictureVo = pictureService.getPictureVo(picture, request);
        pictureVo.setPermissionList(permissionList);
        return ResultUtils.success(pictureVo);
    }

    ;

    /**
     * 分页获取图片列表(管理员)
     *
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPicturePage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表(用户)
     *
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<PictureVo>> listPictureVoPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        //  限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        //  空间权限校验
        Long spaceId=pictureQueryRequest.getSpaceId();
        if(spaceId==null){
            //  公开图库
            //  普通用户默认只能看到审核通过的数据
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        }else{
            //  私有空间
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission,ErrorCode.NO_AUTH_ERROR);
//            User loginUser=userService.getLoginUser(request);
//            Space space=spaceService.getById(spaceId);
//            ThrowUtils.throwIf(space==null,ErrorCode.NOT_FOUND_ERROR,"空间不存在");
//            if(!loginUser.getId().equals(space.getUserId())){
//                throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"没有空间权限");
        }

        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryRequest));
        Page<PictureVo> pictureVoPage = pictureService.getPictureVoPage(picturePage, request);
        return ResultUtils.success(pictureVoPage);
    }

    /**
     * 分页获取图片列表(用户，有缓存)
     *
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @Deprecated
    @PostMapping("/list/page/vo/cache")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<PictureVo>> listPictureVoPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        //  限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        //  普通用户默认只能看到审核通过的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        //  查询缓存，缓存中没有，再查数据库
        //  构建缓存的key
        String queryCondition=JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey= DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey=String.format("zshennopicture:listPictureVoPage:%s",hashKey);
        // 1. 先从本地缓存中查询
        String cachedValue=LOCAL_CACHE.getIfPresent(cacheKey);
        if(cachedValue!=null){
            //  如果缓存命中，返回结果
            Page<PictureVo> cachedPage=JSONUtil.toBean(cachedValue,Page.class);
            return ResultUtils.success(cachedPage);
        }
        // 2. 本地缓存未命中，查询Redis分布式缓存
        ValueOperations<String,String> opsForValue= stringRedisTemplate.opsForValue();
        cachedValue=opsForValue.get(cacheKey);
        if(cachedValue!=null){
            //  如果缓存命中，更新本地缓存，返回结果
            LOCAL_CACHE.put(cacheKey,cachedValue);
            Page<PictureVo> cachedPage=JSONUtil.toBean(cachedValue,Page.class);
            return ResultUtils.success(cachedPage);
        }
        // 3. 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryRequest));
        Page<PictureVo> pictureVoPage = pictureService.getPictureVoPage(picturePage, request);
        //  4.更新缓存
        //  更新Redis缓存
        String cacheValue=JSONUtil.toJsonStr(pictureVoPage);
        //  设置缓存过期时间，5-10分钟过期，防止缓存雪崩（大量的key在同一时间过期）
        int cacheExpireTime=300+ RandomUtil.randomInt(0,300);
        opsForValue.set(cacheKey,cacheValue,cacheExpireTime);
        //  写入本地缓存
        LOCAL_CACHE.put(cacheKey,cacheValue);
        return ResultUtils.success(pictureVoPage);
    }


    /**
     * 删除图片
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.deletePicture(loginUser,deleteRequest);
        return ResultUtils.success(true);
    }

    /**
     * 图片编辑
     *
     * @param pictureEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditRequest == null || pictureEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.editPicture(loginUser,pictureEditRequest);
        return ResultUtils.success(true);
    }


    @GetMapping("tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "测试", "选购", "棍子", "大大方方进", "薄纱", "师傅", "徒弟");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setCategoryList(categoryList);
        pictureTagCategory.setTagList(tagList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 图片审核
     * @param pictureReviewRequest
     * @param request
     * @return
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 批量抓取并获取图片
     * @param pictureUploadByBatchRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Integer uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }

    /**
     * 以图搜图
     * @param searchPictureByPictureRequest
     * @return
     */
    @PostMapping("/search/picture")
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody  SearchPictureByPictureRequest searchPictureByPictureRequest) {
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId==null||pictureId<=0,ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture==null,ErrorCode.NOT_FOUND_ERROR);
        List<ImageSearchResult> imageSearchResults = ImageSearchApiFacade.searchImage(picture.getUrl());
        return ResultUtils.success(imageSearchResults);
    }


    /**
     * 按照颜色搜图
     */
    @PostMapping("/search/color")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<PictureVo>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest,HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser=userService.getLoginUser(request);
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        String picColor = searchPictureByColorRequest.getPicColor();
        List<PictureVo> pictureVoList = pictureService.searchPictureByColor(spaceId,picColor,loginUser);
        return ResultUtils.success(pictureVoList);
    }

    /**
     * 批量编辑
     * @param pictureEditByBatchRequest
     * @param request
     * @return
     */
    @PostMapping("/edit/batch")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPictureByBatch(@RequestBody  PictureEditByBatchRequest pictureEditByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser=userService.getLoginUser(request);
        pictureService.editPictureByBatch(pictureEditByBatchRequest,loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 创建 AI 扩图任务
     * @param createPictureOutPaintingTaskRequest
     * @param request
     * @return
     */
    @PostMapping("/out_painting/create_task")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(@RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
                                                                                    HttpServletRequest request){
        ThrowUtils.throwIf(createPictureOutPaintingTaskRequest==null
                ||createPictureOutPaintingTaskRequest.getPictureId()==null,
                ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        CreateOutPaintingTaskResponse response = pictureService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest,loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 查询 AI 扩图任务
     * @param taskId
     * @return
     */
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId){
        ThrowUtils.throwIf(StrUtil.isBlank(taskId),ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskResponse response = aliYunAiApi.getOutPaintingTask(taskId);
        return ResultUtils.success(response);
    }



}
