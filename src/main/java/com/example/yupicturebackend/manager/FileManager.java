package com.example.yupicturebackend.manager;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.example.yupicturebackend.common.ResultUtils;
import com.example.yupicturebackend.config.CosClientConfig;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.exception.ThrowUtils;
import com.example.yupicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.print.DocFlavor;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
@Deprecated
/**
 * 已废弃改为使用upload包下的模板方法
 */
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片通用类
     * @param multipartFile
     * @param uploadPathPrefix
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile,String uploadPathPrefix){
        //  校验图片
        validatePicture(multipartFile);
        //  图片上传地址
        String uuid= RandomUtil.randomString(16);
        String originalFileName= multipartFile.getOriginalFilename();
        //  自己拼接文件上传文件路径，而不是使用原始的文件路径，防止根据图片名称拼接的url无法访问
        String uploadFileName=String.format("%s_%s.%s",
                DateUtil.formatDate(new Date()),
                uuid,
                FileUtil.getSuffix(originalFileName));

        String uploadPath=String.format("/%s/%s",uploadPathPrefix,uploadFileName);
        // 解析并返回结果

        File file=null;
        try{
            //  上传文件
            file=File.createTempFile(uploadPath,null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            //  获取图片信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            int picWidth=imageInfo.getWidth();
            int picHeight=imageInfo.getHeight();
            double picScale= NumberUtil.round(picWidth*1.0/picHeight,2).doubleValue();
            UploadPictureResult uploadPictureResult=new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost()+'/'+uploadPath);
            uploadPictureResult.setPicName(uploadFileName);
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            return uploadPictureResult;
        }
        catch (Exception e){
            log.error("图片上传到存储对象失败",e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
        }
        finally {
            //  删除临时文件
            this.deleteTemplateFile(file);

        }

    }


    private void validatePicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile==null, ErrorCode.PARAMS_ERROR);
        //  1.  校验文件大小
        long fileSize= multipartFile.getSize();
        final long ONE_M=1024*1024;
        ThrowUtils.throwIf(fileSize>ONE_M,ErrorCode.PARAMS_ERROR,"图片不能大于2MB");
        //  2.  校验文件后缀
        String fileSuffix= FileUtil.getSuffix(multipartFile.getOriginalFilename());
        List<String> allowedFileSuffix= Arrays.asList("jpeg","jpg","png","webp");
        ThrowUtils.throwIf(!allowedFileSuffix.contains(fileSuffix),ErrorCode.PARAMS_ERROR,"图片格式不正确");

    }

    /**
     * 清除临时文件
     * @param file
     */
    private void deleteTemplateFile(File file) {
        if(file==null){
            return;
        }
        boolean deleteResult=file.delete();
        if(!deleteResult){
            log.error("file delete error,filepath={}",file.getAbsolutePath());
        }

    }

    /**
     * 验证图片url格式是否正确
     * @param fileUrl
     */
    private void validPicture(String fileUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl),ErrorCode.PARAMS_ERROR,"文件地址为空");
        //  校验url格式
        try{
            new URL(fileUrl);
        }
        catch (MalformedURLException e){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件格式地址不正确");
        }
        //  校验url协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://")&&!fileUrl.startsWith("https://"),
                ErrorCode.PARAMS_ERROR,"仅支持HTTP或HTTPS协议的文件地址");
        //  发送head请求验证文件是否存在
        HttpResponse httpResponse=null;
        try {
            httpResponse= HttpUtil.createRequest(Method.HEAD,fileUrl).execute();
            //  未正常返回，无需执行其他判断（有的网站不支持head请求，并不能说明文件不存在)
            if(httpResponse.getStatus()!= HttpStatus.HTTP_OK){
                return;

            }
            //  文件存在，文件类型校验
            String contentType   = httpResponse.header("Content-Type");
            //  不为空才校验是否合法
            if(StrUtil.isNotBlank(contentType)){
                //  允许的图片类型
                final List<String> ALLOW_CONTENT_TYPES=Arrays.asList("image/jpeg","image/jpg","image/png","image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR,"文件类型错误");
                //  文件存在，文件大小校验
                String contentLenghStr=httpResponse.header("Content-Length");
                if(StrUtil.isNotBlank(contentLenghStr)){
                    try{
                        long contentLength=Long.parseLong(contentLenghStr);
                        final long ONE_M=1024*1024;
                        ThrowUtils.throwIf(contentLength>2*ONE_M,ErrorCode.PARAMS_ERROR,
                                "文件大小不能超过2MB");
                    }
                    catch (NumberFormatException e){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件大小格式异常");
                    }
                }
            }
        }
        finally {
            //  释放资源
            if(httpResponse!=null){
                httpResponse.close();
            }
        }


    }
}
