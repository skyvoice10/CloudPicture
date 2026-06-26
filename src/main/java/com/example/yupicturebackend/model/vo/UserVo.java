package com.example.yupicturebackend.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户信息视图(脱敏)
 */
@Data
public class UserVo implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 账号
     */
    private String userAccount;


    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;


    /**
     * 创建时间
     */
    private Date createTime;



    /**
     * 会员过期时间
     */
    private Date vipExpireTime;

    /**
     * 会员兑换码
     */
    private String vipCode;

    /**
     * 会员编号
     */
    private Long vipNumber;

}


