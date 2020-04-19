package cn.edu.bjtu.ebosgwinst.controller;

import cn.edu.bjtu.ebosgwinst.service.LogService;
import cn.edu.bjtu.ebosgwinst.service.Restore;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Api(tags = "网关实例")
@RequestMapping("/api/instance")
@RestController
public class GwInstController {
    @Autowired
    LogService logService;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    Restore restore;
    private static final String commandUrl = "http://localhost:8082/api/command";
    private static final String edgeDeviceUrl = "http://localhost:48081/api/v1/device";
    private static final String edgeDeviceProfileUrl = "http://localhost:48081/api/v1/deviceprofile";
    private static final String edgeDeviceServiceUrl = "http://localhost:48081/api/v1/deviceservice";
    private static final String edgeExportUrl = "http://localhost:48071/api/v1/registration";
    private static final String edgeCoreCommandPing = "http://localhost:48082/api/v1/ping";
    private static final String edgeCoreDataPing = "http://localhost:48080/api/v1/ping";
    private static final String edgeCoreMetaDataPing = "http://localhost:48081/api/v1/ping";

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
    @GetMapping("/state")
    public JSONObject ping() {
        //THIS METHOD IS WAITING TO BE OPTIMIZED, BUT ALL THE PARAMETER WON'T CHANGE.
        JSONObject pong = new JSONObject();
        pong.put("gateway-instance", "ONLINE");
        try {
            restTemplate.getForObject(commandUrl + "/ping", String.class);
            pong.put("command", "ONLINE");
        } catch (Exception e) {
            pong.put("command", "OFFLINE");
        }
        try {
            restTemplate.getForObject(edgeCoreDataPing, String.class);
            pong.put("edgex-core-data", "ONLINE");
        } catch (Exception e) {
            pong.put("edgex-core-data", "OFFLINE");
        }
        try {
            restTemplate.getForObject(edgeCoreMetaDataPing, String.class);
            pong.put("edgex-core-metadata", "ONLINE");
        } catch (Exception e) {
            pong.put("edgex-core-metadata", "OFFLINE");
        }
        try {
            restTemplate.getForObject(edgeCoreCommandPing, String.class);
            pong.put("edgex-core-command", "ONLINE");
        } catch (Exception e) {
            pong.put("edgex-core-command", "OFFLINE");
        }
        return pong;
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
