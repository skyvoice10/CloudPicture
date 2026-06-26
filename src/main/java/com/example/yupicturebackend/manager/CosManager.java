package com.example.yupicturebackend.manager;


import cn.hutool.core.io.FileUtil;
import com.example.yupicturebackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;


import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;


    /**
     * 将本地文件上传到 COS
     * @param key
     * @param file
     * @return
     */
    public PutObjectResult putObject(String key, File file){
        PutObjectRequest putObjectRequest=new PutObjectRequest(cosClientConfig.getBucket(),key,file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 将COS文件下载到 本地
     * @param key
     * @return
     */
    public COSObject getObject(String key){
        GetObjectRequest getObjectRequest=new GetObjectRequest(cosClientConfig.getBucket(),key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传并解析图片的方法
     * @param key key为图片存储的路径包含文件名 /public/userId/fileName
     * @param file
     * @return
     */
    public PutObjectResult putPictureObject(String key, File file){
        PutObjectRequest putObjectRequest=new PutObjectRequest(cosClientConfig.getBucket(),key,file);
        //  对图片进行处理
        PicOperations picOperations=new PicOperations();
        //  1.表视返回原图信息
        picOperations.setIsPicInfo(1);
        //  图片处理规则列表
        List<PicOperations.Rule> ruleList=new ArrayList<>();
        //  图片压缩（转变成webp）格式
        String webpKey= FileUtil.mainName(key)+".webp";
        PicOperations.Rule compressRule=new PicOperations.Rule();
        compressRule.setRule("imageMogr2/format/webp");
        compressRule.setFileId(webpKey);
        compressRule.setBucket(cosClientConfig.getBucket());
        ruleList.add(compressRule);
        //  缩略图处理
        //  只对大于20KB的图进行缩略处理
        if(file.length()>20*1024){
            PicOperations.Rule thumbnailRule=new PicOperations.Rule();
            //  拼接缩略图路径
            String thumbnailKey= FileUtil.mainName(key)+"_thumbnail."+FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            //  缩放规则 /thumbnail/<Width>x<Height>(如果大于原图宽高则不做处理)
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>",256,256));
            ruleList.add(thumbnailRule);
        }
        //  构造处理参数
        picOperations.setRules(ruleList);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 删除cos对象
     * @param key
     * @return
     */
    public void  deleteObject(String key){
        try {
            cosClient.deleteObject(cosClientConfig.getBucket(), key);
            System.out.println("删除成功");
        } catch (CosServiceException e) {
            System.out.println("COS 错误：" + e.getErrorMessage());
        } catch (CosClientException e) {
            e.printStackTrace();
        }


    }

}
