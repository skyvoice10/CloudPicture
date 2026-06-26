package com.example.yupicturebackend.aiVoice.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.example.yupicturebackend.aiVoice.model.CreateVoiceModelRequest;
import com.example.yupicturebackend.aiVoice.model.CreateVoiceRequest;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import okhttp3.*;
import com.example.yupicturebackend.aiVoice.service.AiVoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/aiVoice")
public class AiVoiceController {

    @Autowired
    private AiVoiceService aiVoiceService;

    //  创建模型地址
    public static final String CREATE_VOICE_MODEL_URL = "\n" +
            "https://fishaudio.org/api/open/create-model";

    //  合成语音地址
    public static final String CREATE_VOICE_URL = "https://fishaudio.org/api/open/tts";

    //  API_KEY
    private static final String API_KEY = "be7c1e45e22636aa57334a5b7bc2d321a38d98a12d349a5c3170103962df9b89";

    @RequestMapping("/createAiVoice")
    public void createAiVoice() throws Exception {
        //  先创建模型,并拿到模型id
        String modelId = createVoiceModel("棍狗","D:\\Program Files\\FFOutput\\input.mp3");
        System.out.println(modelId);
        //  再合成语音,并拿到合成的语音的url
        String audioUrl=createVoice(modelId,"大家好啊，我操死你的马");
        System.out.println(audioUrl);
    }

    //  创建声音模型
    public String createVoiceModel(String name,String audioFile) {

        //  创建请求
        HttpRequest request = HttpRequest.post(CREATE_VOICE_MODEL_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .form("name",name)
                .form("audioFiles",new File(audioFile));

        //  发起请求并处理响应
        try (HttpResponse httpResponse = request.execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求异常：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建声音模型异常");
            }
            //  如果成功，则获取模型ID
            String modelId = JSONUtil.parseObj(httpResponse.body()).getStr("modelId");
            //  返回模型id
            return modelId;
        }
    }

    //  合成语音
    public String createVoice(String modelId,String text) {

        //  构建请求参数
        /**
         *         {
         *             "reference_id": string,  // 必填，声音模型 ID
         *                 "text": string,          // 必填，要转换的文本
         *                 "speed": number,         // 可选，语速，范围：0.5-2.0，默认：1
         *                 "volume": number,        // 可选，音量，范围：-20-20，默认：0
         *                 "version": string,       // 可选，TTS 版本。可选："v1"、"v2"、"s1"（传统版本）与 "v3-turbo"、"v3-hd"（V3 版本），默认："v1"
         *                 "format": string,        // 可选，音频格式。可选："mp3"、"wav"、"pcm"，默认："mp3"
         *                 "emotion": string,       // 可选，情绪控制（仅 V3 支持）。可选："happy"、"sad"、"angry"、"fearful"、"disgusted"、"surprised"、"calm"、"auto"，默认："auto"
         *                 "language": string,      // 可选，语言增强（仅 V3 支持）。可选："auto"、"zh"、"en"，默认："auto"
         *                 "cache": boolean         // 可选，false 返回音频流，true 返回音频 URL，默认：false
         *         }
         */
        CreateVoiceRequest createVoiceRequest = new CreateVoiceRequest();
        createVoiceRequest.setReference_id(modelId);
        createVoiceRequest.setText(text);
        createVoiceRequest.setCache(true);

        //  创建请求
        HttpRequest request = HttpRequest.post(CREATE_VOICE_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(createVoiceRequest));

        //  发起请求并处理响应
        try (HttpResponse httpResponse = request.execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求异常：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "合成声音异常");
            }
            //  如果成功，则获取声音url
            String audioUrl = JSONUtil.parseObj(httpResponse.body()).getStr("audio_url");
            //  返回声音url
            return audioUrl;
        }
    }
}

