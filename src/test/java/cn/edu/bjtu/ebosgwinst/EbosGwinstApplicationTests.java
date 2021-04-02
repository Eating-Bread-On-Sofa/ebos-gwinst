package cn.edu.bjtu.ebosgwinst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest
class EbosGwinstApplicationTests {

    @Test
    void contextLoads() {
        MultiValueMap<String,Object> js = new LinkedMultiValueMap<>();
        js.add("result","world");
        JSONObject result = new JSONObject();
        result.put("result","world");
        System.out.println(js);
        System.out.println(result);
    }

}
