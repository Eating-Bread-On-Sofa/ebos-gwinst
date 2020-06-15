package cn.edu.bjtu.ebosgwinst.controller;

import cn.edu.bjtu.ebosgwinst.model.FileDescriptor;
import cn.edu.bjtu.ebosgwinst.model.FileSavingMsg;
import cn.edu.bjtu.ebosgwinst.model.GwBackupInfo;
import cn.edu.bjtu.ebosgwinst.model.GwServState;
import cn.edu.bjtu.ebosgwinst.service.FileService;
import cn.edu.bjtu.ebosgwinst.service.LogService;
import cn.edu.bjtu.ebosgwinst.service.Restore;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    private static final String commandUrl = "http://localhost:8082/api/command";
    private static final String edgeDeviceUrl = "http://localhost:48081/api/v1/device";
    private static final String edgeDeviceProfileUrl = "http://localhost:48081/api/v1/deviceprofile";
    private static final String edgeDeviceServiceUrl = "http://localhost:48081/api/v1/deviceservice";
    private static final String edgeExportUrl = "http://localhost:48071/api/v1/registration";
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
        return fileService.saveFiles(multipartFiles, path);
    }

    @ApiOperation(value = "启动指定微服务")
    @CrossOrigin
    @PutMapping("/service")
    public void startService(@RequestParam String jarName){
        fileService.execJar(jarName);
    }

    @ApiOperation(value = "停止指定微服务")
    @CrossOrigin
    @DeleteMapping("/service")
    public void killService(@RequestParam int port){
        fileService.killProcessByPort(port);
    }

    @ApiOperation(value = "微服务健康检测")
    @CrossOrigin
    @GetMapping("/ping")
    public String ping(){
        return "pong";
    }
}
