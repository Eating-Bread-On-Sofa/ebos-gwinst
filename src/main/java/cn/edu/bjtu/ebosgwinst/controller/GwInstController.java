package cn.edu.bjtu.ebosgwinst.controller;

import cn.edu.bjtu.ebosgwinst.service.LogService;
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
    private String commandUrl = "http://localhost:8082/api/command";

    @CrossOrigin
    @GetMapping()
    public JSONObject getInfo(){
        JSONObject result = new JSONObject();
        JSONArray commandArray = new JSONArray(restTemplate.getForObject(commandUrl,JSONArray.class));
        result.put("command",commandArray);
        return result;
    }

    @CrossOrigin
    @PostMapping()
    public String putInfo(@RequestBody JSONObject info){
        JSONArray commandArray = info.getJSONArray("command");
        return restTemplate.postForObject(commandUrl+"/recover",commandArray,String.class);
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
