package com.example.yupicturebackend.service;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.server.HttpServerRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.model.dto.user.UserQueryRequest;
import com.example.yupicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yupicturebackend.model.vo.LoginUserVo;
import com.example.yupicturebackend.model.vo.UserVo;
import org.springframework.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author admin
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2026-01-14 16:31:45
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    public long userRegister(String userAccount, String userPassword, String checkPassword);

    String getEncryptPassword(String userPassword);

    /**
     * 用户登录信息（脱敏）
     *
     * @param userAccount
     * @param userPassword
     * @return
     */
    public LoginUserVo userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 得到用户信息（脱敏）
     *
     * @param user
     * @return
     */
    UserVo getUserVo(User user);

    /**
     * 得到用户信息列表（脱敏）
     *
     * @param userList
     * @return
     */
    List<UserVo> getUserVoList(List<User> userList);

    /**
     * 得到用户登录信息（脱敏）
     *
     * @param user
     * @return
     */
    LoginUserVo getLoginUserVo(User user);

    /**
     * 内部获取用户信息
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户退出登录
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 用户查询条件
     *
     * @param queryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest queryRequest);

    /**
     * 判断用户是否为管理员
     * @param user
     * @return
     */
    public  boolean isAdmin(User user);
}
