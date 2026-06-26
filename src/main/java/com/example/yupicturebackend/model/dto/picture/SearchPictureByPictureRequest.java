package com.example.yupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class SearchPictureByPictureRequest implements Serializable {
    /**
     * 图片id
     */
    private Long pictureId;

    private static final long serialVersionUID = 1065608666849414108L;
}
