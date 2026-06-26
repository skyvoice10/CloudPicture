package com.example.yupicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import com.example.yupicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * 文件图片上传
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    protected void validatePicture(Object inputSource) {
        String fileUrl=(String) inputSource;
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
            //  如果是pivixc的图片url
            if(fileUrl.contains("o.edcms.pw")||fileUrl.contains("i.pximg.net")){
                //  Java → 127.0.0.1:7897 → 节点 → Pixiv CDN
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7897));
                fileUrl=fileUrl.replace("o.edcms.pw","i.pximg.net");
                System.out.println(fileUrl);
                httpResponse=HttpUtil.createRequest(Method.HEAD, fileUrl)
                        .setProxy(proxy)
                        .header("Referer", "https://www.pixiv.net/")
                        .header("User-Agent", "Mozilla/5.0")
                        .timeout(5000)
                        .execute();
            }
            else{
                httpResponse= HttpUtil.createRequest(Method.HEAD,fileUrl).execute();
            }


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
                        ThrowUtils.throwIf(contentLength>10*ONE_M,ErrorCode.PARAMS_ERROR,
                                "文件大小不能超过10MB");
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


    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl=(String) inputSource;
        return FileUtil.mainName(fileUrl);

    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception{

        String fileUrl=(String) inputSource;

        //  处理pivixc的图片url
        if(fileUrl.contains("o.edcms.pw")||fileUrl.contains("i.pximg.net")){
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7897));
            fileUrl=fileUrl.replace("o.edcms.pw","i.pximg.net");
            HttpUtil.createGet(fileUrl)
                    .setProxy(proxy)
                    .header("Referer", "https://www.pixiv.net/")
                    .header("User-Agent", "Mozilla/5.0")
                    .execute()
                    .writeBody(file);
        }
        else{
            //  下载文件到临时文件
            HttpUtil.downloadFile(fileUrl,file);

        }







    }
    public static void main(String[] args){
        String a="12";
        a=a.replace("4","3");
        System.out.println(a);

    }
}
