package com.example.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.example.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.example.yupicturebackend.common.DeleteRequest;
import com.example.yupicturebackend.model.dto.picture.*;
import com.example.yupicturebackend.model.dto.user.UserQueryRequest;
import com.example.yupicturebackend.model.entity.Picture;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yupicturebackend.model.entity.User;
import com.example.yupicturebackend.model.vo.PictureVo;
import io.swagger.models.auth.In;
import org.springframework.web.multipart.MultipartFile;

import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author admin
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2026-01-16 11:53:20
*/
public interface PictureService extends IService<Picture> {
    /**
     * 上传图片
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    public PictureVo uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 图片查询条件
     * @param pictureQueryRequest
     * @return
     */
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 得到图片包装类
     * @param picture
     * @param request
     * @return
     */
    PictureVo getPictureVo(Picture picture, HttpServletRequest request);

    /**
     * 校验图片
     * @param picture
     */
    void validatePicture(Picture picture);

    /**
     * 分页获取图片包装
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVo> getPictureVoPage(Page<Picture> picturePage,HttpServletRequest request);

    /**
     * 图片审核
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest,User loginUser);

    /**
     * 填充编辑字段
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture,User loginUser);

    /**
     * 批量上传图片
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest,User loginUser);

    /**
     * 清理云端的图片文件
     * @param oldPicture
     */
    void clearPictureFile(Picture oldPicture);

    /**
     * 校验图片权限
     * @param loginUser
     * @param picture
     */
    void checkPictureAuth(User loginUser,Picture picture);

    /**
     * 编辑图片
     * @param loginUser
     * @param pictureEditRequest
     */
    void editPicture(User loginUser, PictureEditRequest pictureEditRequest);

    /**
     * 删除图片
     * @param loginUser
     * @param deleteRequest
     */
    void deletePicture(User loginUser, DeleteRequest deleteRequest);

    /**
     * 根据颜色搜索图片
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */
    List<PictureVo>  searchPictureByColor(Long spaceId,String picColor,User loginUser);

    /**
     * 批量编辑图片
     * @param pictureEditByBatchRequest
     * @param loginUser
     */
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest,User loginUser);


    /**
     * 创建扩图任务请求
     * @param createPictureOutPaintingTaskRequest
     * @param loginUser
     * @return
     */
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);
}
