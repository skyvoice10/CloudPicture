package com.example.yupicturebackend.api.imageSearch.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class ImageSearchResult {
    /**
     * 缩略图地址
     */
    private String thumbUrl;

    /**
     * 来源地址
     */
    private String fromUrl;

}
