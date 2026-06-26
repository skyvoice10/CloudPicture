package com.example.yupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {

    private static final long serialVersionUID = 1065608666849414108L;

    /**
     * 图片id用于修改
     */
    private Long id;

    /**
     * 文件url
     */
    private String fileUrl;


    /**
     * 可以上传文件名
     */
    private String picName;

    /**
     * 空间id
     */
    private Long spaceId;





}
