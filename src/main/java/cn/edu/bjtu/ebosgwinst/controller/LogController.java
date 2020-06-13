package cn.edu.bjtu.ebosgwinst.controller;

import cn.edu.bjtu.ebosgwinst.service.LogService;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RequestMapping("/api")
@RestController
public class LogController {
    @Autowired
    LogService logService;

    @CrossOrigin
    @RequestMapping ("/logtest")
    public String logTest(){
        logService.debug("create","gwinst1");
        logService.info("delete","gwinst2");
        logService.warn("update","gwinst3");
        logService.error("retrieve","gwinst4");
        logService.debug("retrieve","增");
        logService.info("update","删");
        logService.warn("delete","改");
        logService.error("create","查");
        return "成功";
    }

    @CrossOrigin
    @GetMapping("/logtest")
    public JSONArray loggerTest(){
        return logService.findAll();
    }

    @CrossOrigin
    @RequestMapping(value = "/log",method = RequestMethod.GET)
    public JSONArray logTest(Date firstDate, Date lastDate, String source, String category,String operation) throws ParseException {
        SimpleDateFormat df =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat ds =  new SimpleDateFormat("yyyy-MM-dd ");
        Date startDate = df.parse(ds.format(firstDate)+"00:00:00");
        Date endDate = df.parse(ds.format(lastDate)+"23:59:59");
        return logService.find(startDate, endDate, source, category,operation);
    }
}

