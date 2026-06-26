package com.example.yupicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件图片上传
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Override
    protected void validatePicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR);
        //  1.  校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024;
        ThrowUtils.throwIf(fileSize > 10 * ONE_M, ErrorCode.PARAMS_ERROR, "图片不能大于10MB");
        //  2.  校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        List<String> allowedFileSuffix = Arrays.asList("jpeg", "jpg", "png", "webp");
        ThrowUtils.throwIf(!allowedFileSuffix.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "图片格式不正确");
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }


    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("2");
        List<String> collect = list.stream().filter(s -> s != "2").collect(Collectors.toList());
        for(int i=0;i<list.size();i++){
            if(list.get(i).equals("1")){
                list.remove(i);

            }

            System.out.println(list.get(i));
        }
    }
}
