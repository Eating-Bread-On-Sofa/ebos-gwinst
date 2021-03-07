package cn.edu.bjtu.ebosgwinst.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Order(1)
public class Init implements ApplicationRunner {
    @Value("${export.name}")
    private String name;
    @Value("${export.ip}")
    private String ip;
    @Value("${export.port}")
    private int port;
    @Value("${export.publisher}")
    private String publisher;
    @Value("${export.user}")
    private String user;
    @Value("${export.password}")
    private String password;
    @Value("${export.topic}")
    private String topic;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LogService logService;
    @Value("${docker}")
    private static String docker ;

    private String url = "http://"+docker+":48071/api/v1/registration";

    @Override
    public void run(ApplicationArguments arguments) {
        new Thread(() -> {
            JSONObject export = new JSONObject();
            JSONObject addressable = new JSONObject();
            addressable.put("name","mqtt");
            addressable.put("protocol","tcp");
            addressable.put("address",ip);
            addressable.put("port",port);
            addressable.put("publisher",publisher);
            addressable.put("user",user);
            addressable.put("password",password);
            addressable.put("topic",topic);
            export.put("name",name);
            export.put("addressable",addressable);
            export.put("format","JSON");
            export.put("enable",true);
            export.put("destination","MQTT_TOPIC");
            try {
                String reply = restTemplate.postForObject(url, export, String.class);
                logService.info("create","新增edgex导出层信息 id:"+reply);
            }
            catch (HttpClientErrorException.BadRequest e){
                restTemplate.put(url, export);
                logService.info("update","edgex导出层信息已按配置文件更新");
            }
            catch (Exception e){
                logService.error("create",e.getMessage());
            }
        }).start();
    }
}
