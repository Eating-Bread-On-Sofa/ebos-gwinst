package cn.edu.bjtu.ebosgwinst.service.impl;

import cn.edu.bjtu.ebosgwinst.model.FileDescriptor;
import cn.edu.bjtu.ebosgwinst.model.FileSavingMsg;
import cn.edu.bjtu.ebosgwinst.service.FileService;
import cn.edu.bjtu.ebosgwinst.service.LogService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LogService logService;

    @Override
    public String getThisJarPath(){
        ApplicationHome home = new ApplicationHome(getClass());
        File jar = home.getSource();
        return jar.getParentFile().toString();
    }

    @Override
    public List<FileSavingMsg> saveFiles(MultipartFile[] multipartFiles, String path){
        List<FileSavingMsg> fileSavingMsgs = new LinkedList<>();
        for (MultipartFile file: multipartFiles) {
            String name = file.getOriginalFilename();
            try {
                FileUtils.copyInputStreamToFile(file.getInputStream(), new File(path +File.separator + name));
                FileSavingMsg fileSavingMsg = new FileSavingMsg(name,"success",true);
                logService.info("create",fileSavingMsg.toString());
                fileSavingMsgs.add(fileSavingMsg);
            }catch (IOException e){
                System.out.println(e.toString());
                FileSavingMsg fileSavingMsg = new FileSavingMsg(name,e.getMessage(),false);
                logService.info("create",fileSavingMsg.toString());
                fileSavingMsgs.add(fileSavingMsg);
            }
        }
        return fileSavingMsgs;
    }

    @Override
    public List<FileDescriptor> getFileList(String path, String[] extensions){
        List<FileDescriptor> fileDescriptors = new LinkedList<>();
        File dir = new File(path);
        Iterator<File> fileIterator = FileUtils.iterateFiles(dir,extensions,false);
        while (fileIterator.hasNext()){
            File file = fileIterator.next();
            FileDescriptor fileDescriptor = new FileDescriptor(file.getName(),FilenameUtils.getExtension(file.getName()));
            fileDescriptors.add(fileDescriptor);
        }
        return fileDescriptors;
    }
//
//    public List<FileDescriptor> translate(List<FileDescriptor> files){
//        for(FileDescriptor file : files){
//            if(file.getName().equals("ebos-commandconfig.jar")){
//                file.setName("指令管理");
//            }
//            if(file.getName().equals("ebos-command.jar")){
//                file.setName("网关指令");
//            }
//            if(file.getName().equals("ebos-gatewayconfig.jar")){
//                file.setName("设备管理");
//            }
//            if(file.getName().equals("ebos-gwinst.jar")){
//                file.setName("网关管理");
//            }
//            if(file.getName().equals("ebos-gatewaymodify.jar")){
//                file.setName("网关配置");
//            }
//            if(file.getName().equals("ebos-gwinst.jar")){
//                file.setName("网关实例");
//            }
//            if(file.getName().equals("ebos-mqrouter.jar")){
//                file.setName("消息路由");
//            }
//            if(file.getName().equals("ebos-oam.jar")){
//                file.setName("运维监控");
//            }
//            if(file.getName().equals("ebos-profile.jar")){
//                file.setName("模板管理");
//            }
//            if(file.getName().equals("ebos-rule.jar")){
//                file.setName("规则引擎");
//            }
//            if(file.getName().equals("ebos-ruleselect.jar")){
//                file.setName("规则管理");
//            }
//            if(file.getName().equals("ebos-scenario.jar")){
//                file.setName("场景管理");
//            }
//            if(file.getName().equals("ebos-servmgmt.jar")){
//                file.setName("服务管理");
//            }
//            if(file.getName().equals("ebos-user.jar")){
//                file.setName("用户管理");
//            }
//        }
//        return files;
//    }

    @Override
    public List<FileSavingMsg> sendFiles(String url, String path, String[] names){
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        for (String name:names) {
            FileSystemResource fileSystemResource = new FileSystemResource(path+File.separator+name);
            paramMap.add("file", fileSystemResource);
        }
        try {
            @SuppressWarnings("unchecked")
            List<FileSavingMsg> fileSavingMsgs = restTemplate.postForObject(url, paramMap, List.class);
            return fileSavingMsgs;
        }catch (Exception e){
            e.printStackTrace();
            FileSavingMsg fileSavingMsg = new FileSavingMsg(Arrays.toString(names),e.getMessage(),false);
            List<FileSavingMsg> fileSavingMsgs = new LinkedList<>();
            fileSavingMsgs.add(fileSavingMsg);
            return fileSavingMsgs;
        }
    }

    @Override
    public void execJar(String name){
        new Thread(() -> {
            try {
                String[] commands = {"java", "-Xmx100m", "-Xss256k", "-jar", name};
                Process process = Runtime.getRuntime().exec(commands,null,new File(getThisJarPath()));
                InputStream inputStream = process.getInputStream();
                InputStream inputStream1 = process.getErrorStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(inputStream1));
                String line=null;
                String line1 = null;
                while(( line = bufferedReader.readLine() )!=null || ( line1 = bufferedReader1.readLine() )!=null) {
                    if (line != null) {
                        System.out.println(line);
                    } else {
                        System.out.println(line1);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void killProcessByPort(int port){
        try {
            Process process = Runtime.getRuntime().exec("lsof -ti:"+port);
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            Process process1 = Runtime.getRuntime().exec("kill "+bufferedReader.readLine());
            bufferedReader.close();
            process.waitFor();
            process.destroy();
            process1.waitFor();
            process1.destroy();
        }catch (Exception ignored){}
    }
}
