package com.example.yupicturebackend.api.imageSearch;

import com.example.yupicturebackend.api.imageSearch.model.ImageSearchResult;
import com.example.yupicturebackend.api.imageSearch.sub.GetImageFirstUrlApi;
import com.example.yupicturebackend.api.imageSearch.sub.GetImageListApi;
import com.example.yupicturebackend.api.imageSearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ImageSearchApiFacade {

    public static List<ImageSearchResult> searchImage(String imgUrl){
        String imagePageUrl= GetImagePageUrlApi.getImagePageUrl(imgUrl);
        String imageFirstUrl= GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList= GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }
    public static void main(String[] args){
        List<ImageSearchResult> imageList=searchImage("https://zshennopicture-1396129987.cos.ap-shanghai.myqcloud.com/public/2011622847102169090/2026-02-03_nAsNja3Zw6tVk5rF.webp");
        System.out.println("结果列表:"+imageList);
    }
}
