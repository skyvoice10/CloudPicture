package com.example.yupicturebackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求类
 */
@Data
public class PageRequest implements Serializable {

    private int current=1;

    private int pageSize=10;

    private String sortField;

    private String sortOrder="descend";

}
