package com.example.yupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadByBatchRequest implements Serializable {

    private static final long serialVersionUID = 1065608666849414108L;

    /**
     *  搜索词
     */
    private String searchText;

    /**
     * 抓取图片数（默认10条）
     */
    private Integer count=10;


    /**
     *  图片名称前缀
     */
    private String namePrefix;



}
