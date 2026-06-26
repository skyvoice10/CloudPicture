package com.example.yupicturebackend.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.example.yupicturebackend.model.entity.Picture;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 图片信息视图(脱敏)
 */
@Data
public class PictureVo implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private Long id;

    /**
     * 图片 url
     */
    private String url;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（JSON 数组）
     */
    private List<String> tags;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户信息
     */
    private  UserVo userVo;

    /**
     * 缩略图 url
     */
    private String thumbnailUrl;

    /**
     * 空间 id（为空表示公共空间）
     */
    private Long spaceId;


    /**
     * 图片主色调
     */
    private String picColor;

    /**
     * 权限列表
     */
    private List<String> permissionList = new ArrayList<>();


    /**
     * 封装类转对象
     */
    public static Picture voToObj(PictureVo pictureVo){
        if(pictureVo==null){
            return null;
        }
        Picture picture=new Picture();
        BeanUtils.copyProperties(pictureVo,picture);
        //  类型不同需要转换
        picture.setTags(JSONUtil.toJsonStr(pictureVo.getTags()));
        return picture;
    }

    /**
     * 对象转封装类
     */
    public static PictureVo objToVo(Picture picture){
        if(picture==null){
            return null;
        }
        PictureVo pictureVo=new PictureVo();
        BeanUtils.copyProperties(picture,pictureVo);
        //  类型不同需要转换
        pictureVo.setTags(JSONUtil.toList(picture.getTags(),String.class));
        return pictureVo;
    }

//    /**
//     * 是否删除
//     */
//    @TableLogic
//    private Integer isDelete;
//
//    /**
//     * 审核状态：0-待审核; 1-通过; 2-拒绝
//     */
//    private Integer reviewStatus;
//
//    /**
//     * 审核信息
//     */
//    private String reviewMessage;
//
//    /**
//     * 审核人 ID
//     */
//    private Long reviewerId;
//
//    /**
//     * 审核时间
//     */
//    private Date reviewTime;
//
//    /**
//     * 缩略图 url
//     */
//    private String thumbnailUrl;
//



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
