package com.example.yupicturebackend.manager.upload;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.example.yupicturebackend.config.CosClientConfig;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.exception.ThrowUtils;
import com.example.yupicturebackend.manager.CosManager;
import com.example.yupicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
/**
 * 图片上传模板
 */
public abstract class PictureUploadTemplate {

    /**
     * 校验输入源(本地文件或URL)
     *
     * @param inputSource
     */
    protected abstract void validatePicture(Object inputSource) ;

    /**
     * 获取输入源的原始文件名
     *
     * @param inputSource
     * @return
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 处理输入源并生成本地临时文件
     *
     * @param inputSource
     * @param file
     */
    protected abstract void processFile(Object inputSource, File file) throws Exception;

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片通用类
     *
     * @param inputSource
     * @param uploadPathPrefix
     * @return
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1. 校验图片
        validatePicture(inputSource);
        // 2. 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFileName = getOriginalFilename(inputSource);
        //  自己拼接文件上传文件路径，而不是使用原始的文件路径，防止根据图片名称拼接的url无法访问
        String uploadFileName = String.format("%s_%s.%s",
                DateUtil.formatDate(new Date()),
                uuid,
                FileUtil.getSuffix(originalFileName));

        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        // 解析并返回结果

        File file = null;
        try {
            // 3. 创建临时文件，获取文件到服务器
            file = File.createTempFile(uploadPath, null);
            processFile(inputSource, file);
            // 4. 上传图片到对象存储
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 5. 获取图片信息对象,封装返回结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //  获取到webp格式文件的处理结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                //  得到压缩后得到的文件信息
                CIObject compressedCiObject = objectList.get(0);
                //  缩略图默认等于压缩后的图
                CIObject thumbnailCiObject=compressedCiObject;
                //  如果进行了缩略处理，得到缩略后的图片信息
                if (objectList.size() > 1) {
                     thumbnailCiObject = objectList.get(1);
                }
                //  封装压缩图以及缩略图的返回结果
                return buildResult(originalFileName, compressedCiObject, thumbnailCiObject,imageInfo);
            }
            return buildResult(originalFileName, uploadPath, file, imageInfo);
        } catch (Exception e) {
            log.error("图片上传到存储对象失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 6. 删除临时文件
            this.deleteTemplateFile(file);

        }

    }

    /**
     * 封装压缩后的返回结果
     *
     * @param originalFileName
     * @param compressedCiObject
     * @param thumbnailCiObject
     * @return
     */
    private UploadPictureResult buildResult(String originalFileName, CIObject compressedCiObject, CIObject thumbnailCiObject,ImageInfo imageInfo) {
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        //  设置压缩后的原图地址
        uploadPictureResult.setUrl(cosClientConfig.getHost() + '/' + compressedCiObject.getKey());
        uploadPictureResult.setPicName(FileUtil.mainName(originalFileName));
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        uploadPictureResult.setPicColor(imageInfo.getAve());
        //  设置缩略图地址
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + '/' + thumbnailCiObject.getKey());
        return uploadPictureResult;
    }

    /**
     * 封装返回结果
     *
     * @param originalFileName
     * @param uploadPath
     * @param file
     * @param imageInfo
     * @return
     */
    private UploadPictureResult buildResult(String originalFileName, String uploadPath, File file, ImageInfo imageInfo) {
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + '/' + uploadPath);
        uploadPictureResult.setPicName(originalFileName);
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        uploadPictureResult.setPicColor(imageInfo.getAve());
        return uploadPictureResult;
    }


    /**
     * 清除临时文件
     *
     * @param file
     */
    private void deleteTemplateFile(File file) {
        if (file == null) {
            return;
        }
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error,filepath={}", file.getAbsolutePath());
        }

    }


}
