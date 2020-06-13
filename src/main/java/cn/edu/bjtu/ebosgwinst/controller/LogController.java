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
        logService.debug("gwinst1");
        logService.info("gwinst2");
        logService.warn("gwinst3");
        logService.error("gwinst4");
        logService.create("增");
        logService.delete("删");
        logService.update("改");
        logService.retrieve("查");
        return "成功";
    }

    @CrossOrigin
    @GetMapping("/logtest")
    public JSONArray loggerTest(){
        return logService.findAll();
    }

    @CrossOrigin
    @RequestMapping(value = "/log",method = RequestMethod.GET)
    public JSONArray logTest(Date fisrtDate, Date lastDate, String source, String category,String function) throws ParseException {
        SimpleDateFormat df =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat ds =  new SimpleDateFormat("yyyy-MM-dd ");
        Date startDate = df.parse(ds.format(fisrtDate)+"00:00:00");
        Date endDate = df.parse(ds.format(lastDate)+"23:59:59");
        return logService.find(startDate, endDate, source, category,function);
    }
}

