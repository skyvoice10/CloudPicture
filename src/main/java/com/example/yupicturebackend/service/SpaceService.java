package com.example.yupicturebackend.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yupicturebackend.model.dto.space.SpaceAddRequest;
import com.example.yupicturebackend.model.dto.space.SpaceQueryRequest;
import com.example.yupicturebackend.model.dto.space.analyze.SpaceAnalyzeRequest;
import com.example.yupicturebackend.model.entity.Picture;
import com.example.yupicturebackend.model.entity.Space;
import com.example.yupicturebackend.model.entity.User;
import com.example.yupicturebackend.model.vo.PictureVo;
import com.example.yupicturebackend.model.vo.SpaceVo;
import com.example.yupicturebackend.model.vo.space.analyze.SpaceUsageAnalyzeResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.SocketHandler;

/**
* @author admin
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2026-02-02 12:02:48
*/
public interface SpaceService extends IService<Space> {


    /**
     * 空间查询条件
     * @param spaceQueryRequest
     * @return
     */
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 得到空间包装类
     * @param space
     * @param request
     * @return
     */
    SpaceVo getSpaceVo(Space space, HttpServletRequest request);

    /**
     * 校验空间
     * @param space
     */
    void validateSpace(Space space,boolean add);

    /**
     * 分页获取空间包装
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVo> getSpaceVoPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 根据空间级别填充空间信息
     * @param space
     */
    public void fillSpaceBySpaceLevel(Space space);

    /**
     * 创建空间
     * @param spaceAddRequest
     * @param loginUser
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 校验空间权限
     * @param loginUser
     * @param space
     */
    void checkSpaceAuth(User loginUser,Space space);


}
