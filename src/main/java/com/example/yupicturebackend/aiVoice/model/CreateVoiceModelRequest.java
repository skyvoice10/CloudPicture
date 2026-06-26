package com.example.yupicturebackend.aiVoice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateVoiceModelRequest {
    //  模型名称
    String name;
    //  模型描述（可选）
    String description;
    //  模型可见性（可选）
    String visibility;
    //  音频文件
    String audioFile;
}
