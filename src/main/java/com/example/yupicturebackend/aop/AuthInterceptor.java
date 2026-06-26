package com.example.yupicturebackend.aop;

import com.example.yupicturebackend.annotation.AuthCheck;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.exception.ThrowUtils;
import com.example.yupicturebackend.model.entity.User;
import com.example.yupicturebackend.model.enums.UserRoleEnum;
import com.example.yupicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {
    @Resource
    UserService userService;

    /**
     * 用户拦截
     * @param joinPoint 切入点
     * @param authCheck 权限检查
     * @return
     * @throws Throwable
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable{
        String mustRole=authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //  获取当前用户
        User user=userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum=UserRoleEnum.getEnumByValue(mustRole);
        //  不需要权限放行
        if(mustRoleEnum==null){
            return joinPoint.proceed();
        }
        //  得到user权限枚举
        UserRoleEnum userRoleEnum=UserRoleEnum.getEnumByValue(user.getUserRole());
        if(userRoleEnum==null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"当前用户权限不足");
        }
        //  要求必须有管理员权限，但用户权限不足
        if(UserRoleEnum.ADMIN.equals(mustRoleEnum)&&!UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"当前用户权限不足");
        }
        return joinPoint.proceed();
    }
}
