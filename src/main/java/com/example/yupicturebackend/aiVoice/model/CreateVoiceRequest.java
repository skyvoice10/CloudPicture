package com.example.yupicturebackend.aiVoice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateVoiceRequest {
    //  声音模型id
    private  String reference_id;
    //  要转换的文本
    private  String text;
    //  返回类型 false: 音频类型 true: json类型
    private boolean cache;
}
