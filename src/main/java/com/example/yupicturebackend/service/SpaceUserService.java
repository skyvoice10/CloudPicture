package com.example.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yupicturebackend.model.dto.space.SpaceAddRequest;
import com.example.yupicturebackend.model.dto.space.SpaceQueryRequest;
import com.example.yupicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.example.yupicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.example.yupicturebackend.model.entity.Space;
import com.example.yupicturebackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yupicturebackend.model.entity.User;
import com.example.yupicturebackend.model.vo.SpaceUserVo;
import com.example.yupicturebackend.model.vo.SpaceVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author admin
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2026-02-10 16:36:57
*/
public interface SpaceUserService extends IService<SpaceUser> {


    /**
     * 空间成员查询条件
     * @param spaceUserQueryRequest
     * @return
     */
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 得到空间成员包装类
     * @param spaceUser
     * @param request
     * @return
     */
    SpaceUserVo getSpaceUserVo(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 校验空间成员
     * @param spaceUser
     */
    void validateSpaceUser(SpaceUser spaceUser,boolean add);

    /**
     * 获取空间成员包装类(列表)
     * @param spaceUserList
     * @return
     */
    List<SpaceUserVo> getSpaceUserVoList(List<SpaceUser> spaceUserList);


    /**
     * 创建空间成员
     * @param spaceUserAddRequest
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);



}
