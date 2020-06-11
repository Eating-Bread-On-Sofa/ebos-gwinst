package cn.edu.bjtu.ebosgwinst.service.impl;

import cn.edu.bjtu.ebosgwinst.service.FileService;
import cn.edu.bjtu.ebosgwinst.service.LogService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import java.util.Iterator;

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
    public JSONObject saveFiles(MultipartFile[] multipartFiles, String path){
        JSONObject result = new JSONObject();
        for (MultipartFile file: multipartFiles) {
            String name = file.getOriginalFilename();
            try {
                FileUtils.copyInputStreamToFile(file.getInputStream(), new File(path +File.separator + name));
                result.put(name,"success");
                System.out.println(name+"success");
            }catch (IOException e){
                System.out.println(e.toString());
                result.put(name,"fail-"+e.getMessage());
                System.out.println(name+"fail");
            }
        }
        return result;
    }

    @Override
    public JSONArray getFileList(String path, String[] extensions){
        JSONArray res = new JSONArray();
        File dir = new File(path);
        Iterator<File> fileIterator = FileUtils.iterateFiles(dir,extensions,true);
        while (fileIterator.hasNext()){
            File file = fileIterator.next();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name",file.getName());
            jsonObject.put("extension", FilenameUtils.getExtension(file.getName()));
            res.add(jsonObject);
        }
        return res;
    }

    @Override
    public JSONObject sendFiles(String url, String path, String[] names){
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        for (String name:names) {
            FileSystemResource fileSystemResource = new FileSystemResource(path+File.separator+name);
            paramMap.add("file", fileSystemResource);
        }
        System.out.println(paramMap);
        try {
            return restTemplate.postForObject(url, paramMap, JSONObject.class);
        }catch (Exception e){
            e.printStackTrace();
            JSONObject res = new JSONObject();
            res.put("error",e.getMessage());
            return res;
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
                new Thread(){}.start();
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
