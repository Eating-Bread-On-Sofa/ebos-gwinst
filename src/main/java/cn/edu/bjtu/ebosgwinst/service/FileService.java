package cn.edu.bjtu.ebosgwinst.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String getThisJarPath();
    JSONObject saveFiles(MultipartFile[] multipartFiles, String path);
    JSONArray getFileList(String path, String[] extensions);
    JSONObject sendFiles(String url, String path, String[] names);
}
