package com.example.yupicturebackend.controller;

import com.example.yupicturebackend.annotation.AuthCheck;
import com.example.yupicturebackend.common.BaseResponse;
import com.example.yupicturebackend.common.ResultUtils;
import com.example.yupicturebackend.constant.UserConstant;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {
    @Resource
    CosManager cosManager;

    /**
     * 上传文件
     * @param multipartFile
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file")MultipartFile multipartFile){
        //  文件名称
        String fileName=multipartFile.getOriginalFilename();
        String filePath=String.format("/test/%s",fileName);
        File file=null;
        try{
            //  上传文件
            file=File.createTempFile(filePath,null);
            multipartFile.transferTo(file);
            cosManager.putObject(filePath,file);
            return ResultUtils.success(filePath);
        }
        catch (Exception e){
            log.error("file upload error ,filePath="+filePath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
        }
        finally {
            if(file!=null){
                //  删除临时文件
                boolean delete=file.delete();
                if(!delete){
                    log.error("file delete error,filepath={}",filePath);
                }
            }
        }
    }

    /**
     * 下载文件
     * @param filepath
     * @param response
     * @throws IOException
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download")
    public void testDownLoadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInputStream=null;
        try{
            COSObject cosObject=cosManager.getObject(filepath);
            cosObjectInputStream=cosObject.getObjectContent();
            byte[] bytes = IOUtils.toByteArray(cosObjectInputStream);
            //  设置响应(自定义response)
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition","attachment;filename="+filepath);
            //  写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        }
        catch(Exception e){
            log.error("file download error ,filePath="+filepath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"下载失败");
        }finally {
            // 关闭流
            if(cosObjectInputStream!=null){
                cosObjectInputStream.close();
            }
        }
    }
}
