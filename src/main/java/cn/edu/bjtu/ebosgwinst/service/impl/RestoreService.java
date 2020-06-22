package cn.edu.bjtu.ebosgwinst.service.impl;

import cn.edu.bjtu.ebosgwinst.service.LogService;
import cn.edu.bjtu.ebosgwinst.service.Restore;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class RestoreService implements Restore {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LogService logService;

    @Override
    public JSONObject restoreEdgeX(JSONObject result, JSONArray jsonArray, String url,String key) {
        if (jsonArray != null) {
            JSONObject reply = new JSONObject();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                try {
                    try {
                        restTemplate.put(url, jsonObject);
                    } catch (HttpClientErrorException.NotFound e) {
                        restTemplate.postForObject(url, jsonObject, String.class);
                    }
                    reply.put(jsonObject.getString("name"), "完成");
                } catch (Exception e) {
                    logService.error(null,e.toString());
                    reply.put(jsonObject.getString("name"), "失败");
                }
            }
            result.put(key,reply);
        }
        return result;
    }
}
