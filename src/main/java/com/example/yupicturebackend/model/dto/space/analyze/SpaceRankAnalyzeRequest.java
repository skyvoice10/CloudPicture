package com.example.yupicturebackend.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 空间使用排行分析请求(仅管理员)
 */
@Data
public class SpaceRankAnalyzeRequest {
    /**
     * 排名前N的空间
     */
    private Integer topN=10;

    private static final long serialVersionUID = 1L;

}
