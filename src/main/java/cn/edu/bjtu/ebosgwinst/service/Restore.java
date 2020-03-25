package cn.edu.bjtu.ebosgwinst.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public interface Restore {
    JSONObject restoreEdgeX(JSONObject result, JSONArray jsonArray, String url,String key);
}
