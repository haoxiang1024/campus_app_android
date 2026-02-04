
package com.hx.campus.utils.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;
import java.util.Map;

public class JsonOperate {
    //    public static String newsKey = "bec633393690881151584f0ce9462ecf";//新闻key
    public static String newsKey = "9fbfe1092fa33bf4bf99d8b6a661963e";//新闻key
    public static String APPID = "nwrkqmmklavajgpp";//新闻接口id
    public static String APPSECRET = "b0MwcllXNVB4eHdBaDN1cFFqQmR0QT09";//新闻接口密钥

    /**
     * @param jsonStr json格式的字符串
     * @param key     要获取值的键
     * @return Object
     * @Title getJsonValueByKey
     * @Description 获取Json格式字符串中key对应的值
     * @version V1.0
     */
//    获取指定key的json数据
    public static Object getJsonValueByKey(String jsonStr, String key) {
        // 此处引入的是 com.alibaba.fastjson.JSONObject; 对象
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        return jsonObject.get(key);
    }

    //根据json数据获取字符数组
    public static String[] getJsonArray(String jsonStr, String key) {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        //转换json数组
        JSONArray jsonArray = jsonObject.getJSONArray(key);
        String[] repGroup = jsonArray.toArray(new String[]{});
        return repGroup;
    }


    /**
     * @param jsonStr 要转换的json对象
     * @return JSONObject 转化为List<Map<String, Object>>:
     */
    public static List<Map<String, Object>> getJsonList(String jsonStr) {
        JSONObject obj = JSONObject.parseObject(jsonStr);
        JSONArray arr = obj.getJSONArray("data");
        String js = JSON.toJSONString(arr, SerializerFeature.WriteClassName);
        List<Map<String, Object>> mapList = JSON.parseObject(js, List.class);
        return mapList;

    }

    /**
     * @param json 要转换的json对象
     * @param c    实体类
     * @return json转list
     */
    public static List getList(String json, Class c) {
        JSONObject jsonObject = JSONObject.parseObject(json);
        String jsonObjectString = jsonObject.getString("data");
        return JSONArray.parseArray(jsonObjectString, c);
    }

    /**
     * @param json 要转换的json对象
     * @param key  key值
     * @return 返回key值对应的string值
     */
    public static String getValue(String json, String key) {
        JSONObject jsonObject = JSONObject.parseObject(json);
        return jsonObject.getString(key);

    }




}
