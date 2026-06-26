package com.example.yupicturebackend.common;

import com.example.yupicturebackend.exception.ErrorCode;
import com.fasterxml.jackson.databind.ser.Serializers;

/**
 * 响应结果工具类
 */
public class ResultUtils {

    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0,data,"ok");
    }

    public static  BaseResponse<?> error(ErrorCode code) {
        return new BaseResponse<>(code);
    }

    public static BaseResponse<?> error(int code,String message){
        return new BaseResponse<>(code,null,message);
    }

    public static BaseResponse<?> error(ErrorCode errorCode,String message){
        return new BaseResponse<>(errorCode.getCode(),null,message);
    }
}
