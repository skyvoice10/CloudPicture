package com.example.yupicturebackend.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yupicturebackend.annotation.AuthCheck;
import com.example.yupicturebackend.common.BaseResponse;
import com.example.yupicturebackend.common.DeleteRequest;
import com.example.yupicturebackend.common.ResultUtils;
import com.example.yupicturebackend.constant.UserConstant;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.exception.ThrowUtils;
import com.example.yupicturebackend.manager.auth.SpaceUserAuthManager;
import com.example.yupicturebackend.model.dto.space.*;
import com.example.yupicturebackend.model.entity.Space;
import com.example.yupicturebackend.model.entity.User;
import com.example.yupicturebackend.model.enums.SpaceLevelEnum;
import com.example.yupicturebackend.model.vo.SpaceVo;
import com.example.yupicturebackend.service.SpaceService;
import com.example.yupicturebackend.service.UserService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/space")
@Slf4j
public class SpaceController {
    @Resource
    SpaceService spaceService;
    @Resource
    UserService userService;
    @Resource
    SpaceUserAuthManager spaceUserAuthManager;


    /**
     * 添加空间
     * @param spaceAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest,HttpServletRequest request){
        ThrowUtils.throwIf(spaceAddRequest==null,ErrorCode.PARAMS_ERROR);
        User loginUser=userService.getLoginUser(request);
        long newId=spaceService.addSpace(spaceAddRequest,loginUser);
        return ResultUtils.success(newId);
    }
    /**
     * 更新空间(仅管理员)
     *
     * @param spaceUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        //  将dto转变成实体类
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        //  数据校验
        spaceService.validateSpace(space,false);
        //  填充参数
        spaceService.fillSpaceBySpaceLevel(space);
        Long id = spaceUpdateRequest.getId();
        //  判断是否存在
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        //  操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取空间(管理员)
     *
     * @param id
     * @return
     */
    @PostMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(space);
    }

    ;

    /**
     * 根据id获取空间(用户)
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<SpaceVo> getSpaceVoById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser=userService.getLoginUser(request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space,loginUser);
        SpaceVo spaceVo = spaceService.getSpaceVo(space, request);
        spaceVo.setPermissionList(permissionList);
        return ResultUtils.success(spaceVo);
    }

    ;

    /**
     * 分页获取空间列表(管理员)
     *
     * @param spaceQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpacePage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();
        Page<Space> spacePage = spaceService.page(new Page<>(current, pageSize),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spacePage);
    }

    /**
     * 分页获取空间列表(用户)
     *
     * @param spaceQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<SpaceVo>> listSpaceVoPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();
        //  限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        Page<Space> spacePage = spaceService.page(new Page<>(current, pageSize),
                spaceService.getQueryWrapper(spaceQueryRequest));
        Page<SpaceVo> spaceVoPage = spaceService.getSpaceVoPage(spacePage, request);
        return ResultUtils.success(spaceVoPage);
    }

    /**
     * 删除空间
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long id = deleteRequest.getId();
        //  判断是否存在
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        //  判断用户是否有权限删除
        spaceService.checkSpaceAuth(loginUser,oldSpace);
        //  操作数据库
        boolean result = spaceService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 空间编辑
     *
     * @param spaceEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceEditRequest == null || spaceEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        //  将dto转变成实体类
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
        //  设置编辑时间
        space.setEditTime(new Date());
        //  数据校验
        spaceService.validateSpace(space,false);
        //  填充数据
        spaceService.fillSpaceBySpaceLevel(space);
        User loginUser = userService.getLoginUser(request);
        Long id = spaceEditRequest.getId();
        //  判断是否存在
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        //  补充审核参数
        //  判断用户是否有权限编辑
        spaceService.checkSpaceAuth(loginUser,oldSpace);
        //  操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel(){
        List<SpaceLevel> spaceLevelList=Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()
                )).collect(Collectors.toList());
          return ResultUtils.success(spaceLevelList);
    }
}
