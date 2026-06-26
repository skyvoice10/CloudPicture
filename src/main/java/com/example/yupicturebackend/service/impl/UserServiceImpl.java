package com.example.yupicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.server.HttpServerRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yupicturebackend.constant.UserConstant;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.exception.ThrowUtils;
import com.example.yupicturebackend.manager.auth.StpKit;
import com.example.yupicturebackend.model.dto.user.UserQueryRequest;
import com.example.yupicturebackend.model.entity.User;
import com.example.yupicturebackend.model.enums.UserRoleEnum;
import com.example.yupicturebackend.model.vo.LoginUserVo;
import com.example.yupicturebackend.model.vo.UserVo;
import com.example.yupicturebackend.service.UserService;
import com.example.yupicturebackend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author admin
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2026-01-14 16:31:45
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {

        //  1.校验参数
        if(StrUtil.hasBlank(userAccount,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }

        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户帐号太短");
        }

        if(userPassword.length()<8||checkPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码太短");
        }

        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入密码不一致");
        }

        //  2.检查用户帐号是否和数据库中的相同
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        long count=this.baseMapper.selectCount(queryWrapper);
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户帐号已存在");
        }

        //  3.密码加密
        String encryptPassword=getEncryptPassword(userPassword);

        //  4.存入数据库中
        User user=new User();
        user.setUserAccount(userAccount);
        user.setUserName("无名");
        user.setUserPassword(encryptPassword);
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult=this.save(user);
        if(!saveResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败，系统出现异常");
        }

        return  user.getId();
    }

    @Override
    public String getEncryptPassword(String userPassword){
        //  加盐，混淆密码
        final String SALT="shinobu";
        return DigestUtils.md5DigestAsHex((userPassword+SALT).getBytes());

    }

    @Override
    public LoginUserVo userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        //  1.校验
        if(StrUtil.hasBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }

        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名错误");
        }

        if(userPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
        }

        //  2.对用户传递的密码进行加密
        String encryptPassword=getEncryptPassword(userPassword);

        //  3.查询数据库中的用户是否存在
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount)
                    .eq("userPassword",encryptPassword);
        User user=this.baseMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(user==null,ErrorCode.PARAMS_ERROR,"用户名或密码错误");

        //  4.保存用户登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE,user);
        //  记录用户登录态到Sa-token，便于空间鉴权时使用，不会影响原来的鉴权
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE,user);
        return this.getLoginUserVo(user);

    }

    @Override
    public UserVo getUserVo(User user) {
        if(user==null){
            return null;
        }
        UserVo userVo= new UserVo();
        BeanUtil.copyProperties(user,userVo);
        return userVo;
    }

    @Override
    public List<UserVo> getUserVoList(List<User> userList) {
        if(CollUtil.isEmpty(userList)){
            return new ArrayList<>();
        }
        return  userList.stream()
                .map(this::getUserVo)
                .collect(Collectors.toList());
    }

    @Override
    public LoginUserVo getLoginUserVo(User user) {
        if(user==null){
            return null;
        }
        LoginUserVo loginUserVo= new LoginUserVo();
        BeanUtil.copyProperties(user,loginUserVo);
        return loginUserVo;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        //  判断是否已登录
        Object userObj=request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user=(User)userObj;
        if(user==null||user.getId()==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"用户未登录");
        }

        //  再查一遍用户防止缓存中的信息和实际的信息不一致
        User currentUser=this.getById(user.getId());
        if(currentUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;



    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        //  判断是否已登录
        Object userObj=request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if(userObj==null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"用户操作错误");
        }
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest queryRequest) {
        if(queryRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数错误");
        }
        Long id = queryRequest.getId();
        String userName = queryRequest.getUserName();
        String userAccount = queryRequest.getUserAccount();
        String userProfile = queryRequest.getUserProfile();
        String userRole = queryRequest.getUserRole();
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id),"id",id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole),"userRole",userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount),"userAccount",userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userProfile),"userProfile",userProfile);
        queryWrapper.like(StrUtil.isNotBlank(userName),"userName",userName);
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField),sortOrder.equals("ascend"),sortField);
        return queryWrapper;
    }

    @Override
    public  boolean isAdmin(User user){
        return user!=null&&UserConstant.ADMIN_ROLE.equals(user.getUserRole());
    }

}




