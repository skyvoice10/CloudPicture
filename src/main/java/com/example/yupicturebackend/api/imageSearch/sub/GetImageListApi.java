package com.example.yupicturebackend.api.imageSearch.sub;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.yupicturebackend.api.imageSearch.model.ImageSearchResult;
import com.example.yupicturebackend.exception.BusinessException;
import com.example.yupicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GetImageListApi {

    public static List<ImageSearchResult>  getImageList(String url){
        try{
            //  发起get请求
            HttpResponse response= HttpUtil.createGet(url).execute();
            //  获取相应内容
            int statusCode=response.getStatus();
            String body = response.body();

            //  处理响应
            if(statusCode==200){
                //  解析json数据并返回
                return processResponse(body);
            }
            else{
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"接口调用失败");
            }
        }
        catch (Exception e){
            log.error("获取图片列表失败"+e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"获取图片列表失败");
        }
    }

    /**
     * 处理接口响应内容
     * @param body
     * @return
     */
    private static List<ImageSearchResult> processResponse(String body) {
        //  解析响应内容
        JSONObject jsonObject=new JSONObject(body);
        if(!jsonObject.containsKey("data")){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"未取得图片列表");
        }
        JSONObject data=jsonObject.getJSONObject("data");
        if(!data.containsKey("list")){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"未取得图片列表");
        }
        JSONArray list=data.getJSONArray("list");
        return JSONUtil.toList(list,ImageSearchResult.class);
    }


    public static void main(String[] args){
        String url="https://graph.baidu.com/ajax/pcsimi?carousel=503&entrance=GENERAL&extUiData%5BisLogoShow%5D=1&inspire=general_pc&limit=30&next=2&render_type=card&session_id=14030732451056532780&sign=121a112b4c44f7ecf44f201770217019&tk=b3210&tpl_from=pc";
        List<ImageSearchResult> imageList = getImageList(url);
        System.out.println(imageList);
    }
}
