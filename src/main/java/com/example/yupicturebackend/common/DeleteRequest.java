package com.example.yupicturebackend.common;

import lombok.Getter;


import java.io.Serializable;

/**
 * 通用删除请求类
 */
@Getter
public class DeleteRequest implements Serializable {

    private  Long id;

    private static final long serialVersionUID=1L;

}
