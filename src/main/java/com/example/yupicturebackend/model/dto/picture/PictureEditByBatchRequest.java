package com.example.yupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
/**
 * 图片批量编辑请求
 */
public class PictureEditByBatchRequest implements Serializable {

    private static final long serialVersionUID = -33024931206253670L;
    /**
     * 图片id列表
     */
    private List<Long> pictureIdList;

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（JSON 数组）
     */
    private List<String> tags;

    /**
     * 命名规则
     */
    private String nameRule;
}
