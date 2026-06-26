package com.example.yupicturebackend.model.dto.space;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.naming.directory.SearchResult;
import java.io.Serializable;

@Data
public class SpaceEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;


    /**
     * 空间名称
     */
    private String spaceName;



}
