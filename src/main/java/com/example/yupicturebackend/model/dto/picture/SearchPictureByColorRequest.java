package com.example.yupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class SearchPictureByColorRequest implements Serializable {
    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 图片颜色
     */
    private String picColor;

    private static final long serialVersionUID = 1065608666849414108L;
}
