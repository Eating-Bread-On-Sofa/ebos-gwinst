package cn.edu.bjtu.ebosgwinst.controller;

import cn.edu.bjtu.ebosgwinst.entity.FileDescriptor;
import cn.edu.bjtu.ebosgwinst.entity.FileSavingMsg;
import cn.edu.bjtu.ebosgwinst.entity.GwServState;
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

    @ApiOperation(value = "从网关备份数据", notes = "数据格式请试试，很多数据来源是edgex，后端没有对应实体")
    @CrossOrigin
    @GetMapping()
    public JSONObject getInfo() {
        JSONObject result = new JSONObject();
        try {
            JSONArray commandArray = new JSONArray(restTemplate.getForObject(commandUrl, JSONArray.class));
            result.put("command", commandArray);
        }catch (Exception ignored){}
        try {
            JSONArray deviceArr = new JSONArray(restTemplate.getForObject(edgeDeviceUrl, JSONArray.class));
            result.put("device", deviceArr);
        }catch (Exception ignored){}
        try {
            JSONArray deviceProfileArr = new JSONArray(restTemplate.getForObject(edgeDeviceProfileUrl, JSONArray.class));
            result.put("deviceprofile", deviceProfileArr);
        }catch (Exception ignored){}
        try {
            JSONArray deviceServiceArr = new JSONArray(restTemplate.getForObject(edgeDeviceServiceUrl, JSONArray.class));
            result.put("deviceservice", deviceServiceArr);
        }catch (Exception ignored){}
        try {
            JSONArray exportArr = new JSONArray(restTemplate.getForObject(edgeExportUrl, JSONArray.class));
            result.put("export", exportArr);
        }catch (Exception ignored){}
        return result;
    }

    @ApiOperation(value = "向网关恢复数据", notes = "数据格式请参考备份API返回值，很多数据来源是edgex，后端没有对应实体")
    @CrossOrigin
    @PostMapping()
    public JSONObject putInfo(@RequestBody JSONObject info) {
        JSONArray commandArray = info.getJSONArray("command");
        JSONArray deviceServiceArr = info.getJSONArray("deviceservice");
        JSONArray exportArr = info.getJSONArray("export");
        JSONObject result = new JSONObject();
        if (commandArray != null) {
            String commandReply = restTemplate.postForObject(commandUrl + "/recover", commandArray, String.class);
            result.put("command", commandReply);
        }
        result = restore.restoreEdgeX(result, deviceServiceArr, edgeDeviceServiceUrl,"deviceservice");
        result = restore.restoreEdgeX(result, exportArr, edgeExportUrl,"export");
        return result;
    }

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

    @CrossOrigin
    @GetMapping("/service")
    public List<FileDescriptor> inquireService(){
        String path = fileService.getThisJarPath();
        return fileService.getFileList(path,new String[]{"jar"});
    }

    @CrossOrigin
    @PostMapping("/service")
    public List<FileSavingMsg> postService(@RequestParam("file") MultipartFile[] multipartFiles){
        String path = fileService.getThisJarPath();
        System.out.println("存储路径:"+path);
        return fileService.saveFiles(multipartFiles, path);
    }

    @CrossOrigin
    @PutMapping("/service")
    public void startService(@RequestParam String jarName){
        fileService.execJar(jarName);
    }

    @CrossOrigin
    @DeleteMapping("/service")
    public void killService(@RequestParam int port){
        fileService.killProcessByPort(port);
    }

    @CrossOrigin
    @GetMapping("/ping")
    public String ping(){
        return "pong";
    }
}
