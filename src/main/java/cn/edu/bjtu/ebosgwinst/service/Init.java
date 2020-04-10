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
    private String url = "http://localhost:48071/api/v1/registration";

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
                System.out.println(reply);
                if (reply.equals("Name already taken")) {
                    restTemplate.put(url, export);
                    System.out.println("if");
                }
            }
            catch (HttpClientErrorException.BadRequest e){
                restTemplate.put(url, export);
                System.out.println("catch");
            }
            catch (Exception e){
                logService.error(e.getMessage());
            }
        }).start();
    }
}
