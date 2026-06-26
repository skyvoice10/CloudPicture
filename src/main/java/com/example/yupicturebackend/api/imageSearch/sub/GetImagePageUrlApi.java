package com.example.yupicturebackend.api.imageSearch.sub;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
/**
 * 获取以图搜图页面地址（step1）
 */
public class GetImagePageUrlApi {
    /**
     * 获取以图搜图页面地址
     *
     * @param imageUrl
     * @return
     */
    public static String getImagePageUrl(String imageUrl) {
//        image: https%3A%2F%2Fzshennopicture-1396129987.cos.ap-shanghai.myqcloud.com%2Fpublic%2F2011622847102169090%2F2026-02-03_nAsNja3Zw6tVk5rF.webp
//        tn: pc
//        from: pc
//        image_source: PC_UPLOAD_URL
//        sdkParams:
        //1.准备请求参数
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        //  获取当前时间戳
        long upTime = System.currentTimeMillis();
        //  请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + upTime;
        try {
            // 2.发起请求
            HttpResponse httpResponse = HttpRequest.post(url)
                    .header("acs-token", RandomUtil.randomString(1))
                    .form(formData)
                    .timeout(5000)
                    .execute();
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            //  解析响应
            //        {"status":0,"msg":"Success","data":{"url":"https://graph.baidu.com/s?,"sign":"1216b91fa93a310d1361601770192857"}}
            String body = httpResponse.body();
            Map<String, Object> result = JSONUtil.toBean(body, Map.class);
            System.out.println(body);
            // 3.处理响应结果
            if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            //  对URL进行解码
            String rawUrl = (String) data.get("url");
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);

            //  如果url为空
            if (StrUtil.isBlank(searchResultUrl)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效的结果地址");
            }
            return searchResultUrl;

        } catch (Exception e) {
            log.error("调用百度以图搜图接口失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

    public static void main(String[] args){
        //  测试以图搜图功能
        String url="https://zshennopicture-1396129987.cos.ap-shanghai.myqcloud.com/space/2018537540836249601/2026-02-05_NezpTkacShsvbE90.webp";
        String imagePageUrl = getImagePageUrl(url);
        System.out.println(imagePageUrl);
    }
    }
