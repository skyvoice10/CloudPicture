package com.example.yupicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yupicturebackend.annotation.AuthCheck;
import com.example.yupicturebackend.common.BaseResponse;
import com.example.yupicturebackend.common.DeleteRequest;
import com.example.yupicturebackend.common.ResultUtils;
import com.example.yupicturebackend.constant.UserConstant;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.exception.ThrowUtils;
import com.example.yupicturebackend.model.dto.user.*;
import com.example.yupicturebackend.model.entity.User;
import com.example.yupicturebackend.model.vo.LoginUserVo;
import com.example.yupicturebackend.model.vo.UserVo;
import com.example.yupicturebackend.service.UserService;
import com.fasterxml.jackson.databind.ser.Serializers;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    UserService userService;

    @PostMapping("/register")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        ThrowUtils.throwIf(userRegisterRequest==null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result=userService.userRegister(userAccount,userPassword,checkPassword);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVo> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        ThrowUtils.throwIf(userLoginRequest==null, ErrorCode.PARAMS_ERROR);
        String userAccount =  userLoginRequest.getUserAccount();
        String userPassword =  userLoginRequest.getUserPassword();
        LoginUserVo loginUserVo=userService.userLogin(userAccount,userPassword,request);
        return ResultUtils.success(loginUserVo);
    }

    @GetMapping("/get/login")
    public BaseResponse<LoginUserVo> getLoginUser(HttpServletRequest request){
        User user=userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVo(user));

    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request){
        boolean result=userService.userLogout(request);
        return ResultUtils.success(result);

    }

    /**
     * 添加用户(管理员)
     * @param userAddRequest
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest){
        ThrowUtils.throwIf(userAddRequest==null,ErrorCode.PARAMS_ERROR);
        User user=new User();
        BeanUtil.copyProperties(userAddRequest,user);
        //  默认密码
        final String password="12345678";
        String encryptPassword=userService.getEncryptPassword(password);
        user.setUserPassword(encryptPassword);
        //  插入数据库
        boolean result=userService.save(user);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());

    }

    /**
     * 根据id得到用户信息(管理员)
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id){
        ThrowUtils.throwIf(id<=0,ErrorCode.PARAMS_ERROR);
        User user=userService.getById(id);
        ThrowUtils.throwIf(user==null,ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据id获取用户视图
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserVo> getUserVoById(long id){
        ThrowUtils.throwIf(id<=0,ErrorCode.PARAMS_ERROR);
        User user=getUserById(id).getData();
        UserVo userVo=userService.getUserVo(user);
        return ResultUtils.success(userVo);
    }

    /**
     * 删除用户
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest){
        ThrowUtils.throwIf(deleteRequest==null||deleteRequest.getId()<=0,ErrorCode.PARAMS_ERROR);
        boolean result=userService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新用户
     * @param userUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest){
        ThrowUtils.throwIf(userUpdateRequest==null,ErrorCode.PARAMS_ERROR);
        User user=new User();
        BeanUtil.copyProperties(userUpdateRequest,user);
        //  插入数据库
        boolean result=userService.updateById(user);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);

    }

    /**
     * 分页获取用户封封装列表(仅管理员)
     * @param userQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVo>> listUserVoByPage(@RequestBody UserQueryRequest userQueryRequest){
        ThrowUtils.throwIf(userQueryRequest==null,ErrorCode.PARAMS_ERROR);;
        long current=userQueryRequest.getCurrent();
        long pageSize=userQueryRequest.getPageSize();
        Page<User> userPage=userService.page(new Page<>(current,pageSize),
                userService.getQueryWrapper(userQueryRequest)) ;
        Page<UserVo> userVoPage=new Page<>(current,pageSize,userPage.getTotal());
        List<UserVo> userVoList=userService.getUserVoList(userPage.getRecords());
        userVoPage.setRecords(userVoList);
        return ResultUtils.success(userVoPage);
    }


}
