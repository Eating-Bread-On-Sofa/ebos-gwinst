package cn.edu.bjtu.ebosgwinst.service.impl;

import cn.edu.bjtu.ebosgwinst.entity.Registration;
import cn.edu.bjtu.ebosgwinst.service.LogService;
import cn.edu.bjtu.ebosgwinst.service.RegistrationService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LogService logService;

    private static String url = "http://localhost:48071/api/v1/registration";

    @Override
    public String registration(Registration registration) {
        JSONObject exportToCloud = new JSONObject();
        JSONObject addressable = new JSONObject();
        JSONObject filter = new JSONObject();
        addressable.put("name","cloud");
        addressable.put("protocol","TCP");
        addressable.put("address",registration.getAddress());
        addressable.put("port",1883);
        addressable.put("publisher",registration.getPublisher());
        addressable.put("user",registration.getAdmin());
        addressable.put("password",registration.getPassword());
        addressable.put("topic",registration.getTopic());
        exportToCloud.put("name",registration.getName());
        exportToCloud.put("addressable",addressable);
        exportToCloud.put("format","JSON");
        if (registration.getDeviceIdentifiers() != null){
            filter.put("deviceIdentifiers",registration.getDeviceIdentifiers());
            exportToCloud.put("filter",filter);
        }
        exportToCloud.put("enable", true);
        exportToCloud.put("destination","MQTT_TOPIC");

        String result = restTemplate.postForObject(url,exportToCloud,String.class);
        logService.info("create","新增edgex导出到云端信息 "+ result);
        return "导出成功";
    }
}
