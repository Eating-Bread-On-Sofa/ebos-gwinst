package cn.edu.bjtu.ebosgwinst.controller;

import cn.edu.bjtu.ebosgwinst.service.LogService;
import cn.edu.bjtu.ebosgwinst.service.Restore;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RequestMapping("/api/instance")
@RestController
public class GwInstController {
    @Autowired
    LogService logService;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    Restore restore;
    private String commandUrl = "http://localhost:8082/api/command";
    private String edgeDeviceUrl = "http://localhost:48081/api/v1/device";
    private String edgeDeviceProfileUrl = "http://localhost:48081/api/v1/deviceprofile";
    private String edgeDeviceServiceUrl = "http://localhost:48081/api/v1/deviceservice";
    private String edgeExportUrl = "http://localhost:48071/api/v1/registration";

    @CrossOrigin
    @GetMapping()
    public JSONObject getInfo() {
        JSONObject result = new JSONObject();
        JSONArray commandArray = new JSONArray(restTemplate.getForObject(commandUrl, JSONArray.class));
        JSONArray deviceArr = new JSONArray(restTemplate.getForObject(edgeDeviceUrl, JSONArray.class));
        JSONArray deviceProfileArr = new JSONArray(restTemplate.getForObject(edgeDeviceProfileUrl, JSONArray.class));
        JSONArray deviceServiceArr = new JSONArray(restTemplate.getForObject(edgeDeviceServiceUrl, JSONArray.class));
        JSONArray exportArr = new JSONArray(restTemplate.getForObject(edgeExportUrl, JSONArray.class));
        result.put("command", commandArray);
        result.put("device", deviceArr);
        result.put("deviceprofile", deviceProfileArr);
        result.put("deviceservice", deviceServiceArr);
        result.put("export", exportArr);
        return result;
    }

    @CrossOrigin
    @PostMapping()
    public JSONObject putInfo(@RequestBody JSONObject info) {
        JSONArray commandArray = info.getJSONArray("command");
        JSONArray deviceProfileArr = info.getJSONArray("deviceprofile");
        JSONArray deviceServiceArr = info.getJSONArray("deviceservice");
        JSONArray exportArr = info.getJSONArray("export");
        JSONObject result = new JSONObject();
        if (commandArray != null) {
            String commandReply = restTemplate.postForObject(commandUrl + "/recover", commandArray, String.class);
            result.put("command", commandReply);
        }
        result = restore.restoreEdgeX(result, deviceProfileArr, edgeDeviceProfileUrl,"deviceprofile");
        result = restore.restoreEdgeX(result, deviceServiceArr, edgeDeviceServiceUrl,"deviceservice");
        result = restore.restoreEdgeX(result, exportArr, edgeExportUrl,"export");
        return result;
    }

    @CrossOrigin
    @GetMapping("/log/info")
    public String getLogInfo(){
        return logService.findLogByCategory("info");
    }

    @CrossOrigin
    @GetMapping("/log")
    public String getLog(){
        return logService.findAll();
    }

    @CrossOrigin
    @GetMapping("/log/warn")
    public String getLogWarn(){
        return logService.findLogByCategory("warn");
    }

    @CrossOrigin
    @GetMapping("/log/error")
    public String getLogError(){
        return logService.findLogByCategory("error");
    }
}
