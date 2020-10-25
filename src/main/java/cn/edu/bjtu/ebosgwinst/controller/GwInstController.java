package cn.edu.bjtu.ebosgwinst.controller;

import cn.edu.bjtu.ebosgwinst.entity.Registration;
import cn.edu.bjtu.ebosgwinst.model.FileDescriptor;
import cn.edu.bjtu.ebosgwinst.model.FileSavingMsg;
import cn.edu.bjtu.ebosgwinst.model.GwBackupInfo;
import cn.edu.bjtu.ebosgwinst.model.GwServState;
import cn.edu.bjtu.ebosgwinst.service.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    FileService fileService;
    @Autowired
    SubscribeService subscribeService;
    @Autowired
    MqFactory mqFactory;
    @Autowired
    RegistrationService registrationService;

    public static final List<RawSubscribe> status = new LinkedList<>();
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 50,3, TimeUnit.SECONDS,new SynchronousQueue<>());

    private static final String commandUrl = "http://localhost:8082/api/command";
    private static final String edgeDeviceUrl = "http://localhost:48081/api/v1/device";
    private static final String edgeDeviceProfileUrl = "http://localhost:48081/api/v1/deviceprofile";
    private static final String edgeDeviceServiceUrl = "http://localhost:48081/api/v1/deviceservice";
    private static final String edgeExportUrl = "http://192.168.81.141:48071/api/v1/registration";
    private static final String edgeCoreCommandPing = "http://localhost:48082/api/v1/ping";
    private static final String edgeCoreDataPing = "http://localhost:48080/api/v1/ping";
    private static final String edgeCoreMetaDataPing = "http://localhost:48081/api/v1/ping";

    @ApiOperation(value = "从网关备份数据", notes = "每项字段具体内容，请参考相应微服务或edgex")
    @CrossOrigin
    @GetMapping()
    public GwBackupInfo getInfo() {
        GwBackupInfo gwBackupInfo = new GwBackupInfo();
        try {
            JSONArray commandArray = new JSONArray(restTemplate.getForObject(commandUrl, JSONArray.class));
            gwBackupInfo.setCommand(commandArray);
        }catch (Exception ignored){}
        try {
            JSONArray deviceArr = new JSONArray(restTemplate.getForObject(edgeDeviceUrl, JSONArray.class));
            gwBackupInfo.setEdgeXDevice(deviceArr);
        }catch (Exception ignored){}
        try {
            JSONArray deviceProfileArr = new JSONArray(restTemplate.getForObject(edgeDeviceProfileUrl, JSONArray.class));
            gwBackupInfo.setEdgeXProfile(deviceProfileArr);
        }catch (Exception ignored){}
        try {
            JSONArray deviceServiceArr = new JSONArray(restTemplate.getForObject(edgeDeviceServiceUrl, JSONArray.class));
            gwBackupInfo.setEdgeXService(deviceServiceArr);
        }catch (Exception ignored){}
        try {
            JSONArray exportArr = new JSONArray(restTemplate.getForObject(edgeExportUrl, JSONArray.class));
            gwBackupInfo.setEdgeXExport(exportArr);
        }catch (Exception ignored){}
        logService.info( "retrieve","查看本网关命令信息、设备信息、设备模板信息、设备服务信息以及导出地址信息");
        return gwBackupInfo;
    }

    @ApiOperation(value = "向网关恢复数据", notes = "每项字段具体内容，请参考相应微服务或edgex")
    @CrossOrigin
    @PostMapping()
    public JSONObject putInfo(@RequestBody GwBackupInfo gwBackupInfo) {
        JSONArray commandArray = gwBackupInfo.getCommand();
        JSONArray deviceServiceArr = gwBackupInfo.getEdgeXService();
        JSONArray exportArr = gwBackupInfo.getEdgeXExport();
        JSONObject result = new JSONObject();
        if (commandArray != null) {
            String commandReply = restTemplate.postForObject(commandUrl + "/recover", commandArray, String.class);
            result.put("command", commandReply);
        }
        result = restore.restoreEdgeX(result, deviceServiceArr, edgeDeviceServiceUrl,"edgeXService");
        result = restore.restoreEdgeX(result, exportArr, edgeExportUrl,"edgeXExport");
        logService.info("update","复原本网关之前的设备命令、设备服务和导出地址信息");
        return result;
    }

    @ApiOperation(value = "查看指定网关微服务运行状态")
    @CrossOrigin
    @GetMapping("/state")
    public GwServState getState() {
        //THIS METHOD IS WAITING TO BE OPTIMIZED, BUT ALL THE PARAMETER WON'T CHANGE.
        GwServState gwServState = new GwServState();
        try {
            restTemplate.getForObject(commandUrl + "/ping", String.class);
            gwServState.setCommand(true);
        } catch (Exception ignored) {
        }
        try {
            restTemplate.getForObject(edgeCoreDataPing, String.class);
            gwServState.setEdgexCoreData(true);
        } catch (Exception ignored) {
        }
        try {
            restTemplate.getForObject(edgeCoreMetaDataPing, String.class);
            gwServState.setEdgexCoreMetadata(true);
        } catch (Exception ignored) {
        }
        try {
            restTemplate.getForObject(edgeCoreCommandPing, String.class);
            gwServState.setEdgexCoreCommand(true);
        } catch (Exception ignored) {
        }
        logService.info("retrieve","检测指令管理、edgex核心数据微服务、edgex元数据微服务以及edgex核心命令微服务的运行状况");
        return gwServState;
    }

    @ApiOperation(value = "查看指定网关所部署的微服务")
    @CrossOrigin
    @GetMapping("/service")
    public List<FileDescriptor> inquireService(){
        String path = fileService.getThisJarPath();
        return fileService.getFileList(path,new String[]{"jar"});
    }

    @ApiOperation(value = "接收下发微服务")
    @CrossOrigin
    @PostMapping("/service")
    public List<FileSavingMsg> postService(@RequestParam("file") MultipartFile[] multipartFiles){
        String path = fileService.getThisJarPath();
        System.out.println("存储路径:"+path);
        logService.info("create","本网关成功接收了下发的微服务");
        return fileService.saveFiles(multipartFiles, path);
    }

    @ApiOperation(value = "启动指定微服务")
    @CrossOrigin
    @PutMapping("/service")
    public void startService(@RequestParam String jarName){
        fileService.execJar(jarName);
        logService.info("update","本网关启动了微服务"+jarName);
    }

    @ApiOperation(value = "停止指定微服务")
    @CrossOrigin
    @DeleteMapping("/service")
    public void killService(@RequestParam int port){
        fileService.killProcessByPort(port);
        logService.info("update","本网关终止了端口为"+port+"的微服务的运行");
    }

    @ApiOperation(value = "微服务订阅mq的主题")
    @CrossOrigin
    @PostMapping("/subscribe")
    public String newSubscribe(RawSubscribe rawSubscribe){
        if(!GwInstController.check(rawSubscribe.getSubTopic())){
            try{
                status.add(rawSubscribe);
                subscribeService.save(rawSubscribe.getSubTopic());
                threadPoolExecutor.execute(rawSubscribe);
                logService.info("create","网关实例成功订阅主题"+ rawSubscribe.getSubTopic());
                return "订阅成功";
            }catch (Exception e) {
                e.printStackTrace();
                logService.error("create","网关实例订阅主题"+rawSubscribe.getSubTopic()+"时，参数设定有误。");
                return "参数错误!";
            }
        }else {
            logService.error("create","网关实例已订阅主题"+rawSubscribe.getSubTopic()+",再次订阅失败");
            return "订阅主题重复";
        }
    }

    public static boolean check(String subTopic){
        boolean flag = false;
        for (RawSubscribe rawSubscribe : status) {
            if(subTopic.equals(rawSubscribe.getSubTopic())){
                flag=true;
                break;
            }
        }
        return flag;
    }

    @ApiOperation(value = "删除微服务订阅mq的主题")
    @CrossOrigin
    @DeleteMapping("/subscribe/{subTopic}")
    public boolean delete(@PathVariable String subTopic){
        boolean flag;
        synchronized (status){
            flag = status.remove(search(subTopic));
        }
        return flag;
    }

    public static RawSubscribe search(String subTopic){
        for (RawSubscribe rawSubscribe : status) {
            if(subTopic.equals(rawSubscribe.getSubTopic())){
                return rawSubscribe;
            }
        }
        return null;
    }

    @ApiOperation(value = "微服务向mq的某主题发布消息")
    @CrossOrigin
    @PostMapping("/publish")
    public String publish(@RequestParam(value = "topic") String topic,@RequestParam(value = "message") String message){
        MqProducer mqProducer = mqFactory.createProducer();
        mqProducer.publish(topic,message);
        return "发布成功";
    }

    @ApiOperation(value = "将云端信息注册到边缘端（边缘设备的数据就会导出到云端）")
    @CrossOrigin
    @PostMapping("/export")
    public String export(Registration registration){
        String info = registrationService.registration(registration);
        return info;
    }

    @ApiOperation(value = "查看注册的云端信息")
    @CrossOrigin
    @GetMapping("/export")
    public JSONArray getExportInfo(){
        JSONArray exportInfo = restTemplate.getForObject(edgeExportUrl,JSONArray.class);
        return exportInfo;
    }

    @ApiOperation(value = "注销掉某条注册的云端信息")
    @CrossOrigin
    @DeleteMapping("/export/{name}")
    public String delExportInfo(@PathVariable String name){
        String url = edgeExportUrl + "/name/" + name;
        restTemplate.delete(url);
        return "删除成功";
    }

    @ApiOperation(value = "微服务健康检测")
    @CrossOrigin
    @GetMapping("/ping")
    public String ping(){
        logService.info("retrieve","对网关实例进行了一次健康检测");
        return "pong";
    }
}
