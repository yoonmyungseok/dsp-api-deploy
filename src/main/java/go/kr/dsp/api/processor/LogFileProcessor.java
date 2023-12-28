package go.kr.dsp.api.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.converter.stream.InputStreamCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogFileProcessor implements Processor {
    
    @Value("${spring.config.activate.on-profile}")
    private String logName;
    
    @Override
    public void process(Exchange exchange) throws Exception {
        String jsonStr = "";
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> processResult = new HashMap<>();
        
        //스크립트 내용, 실행 결과
        if (exchange.getMessage().getHeader("fileExtension", String.class).equals("sh")) {
            Object body = exchange.getMessage().getBody();
            String dataString = "";
            if (body instanceof InputStreamCache) {
                dataString = new String(((InputStreamCache) body).readAllBytes(), StandardCharsets.UTF_8);
            } else {
                dataString = body.toString();
            }
            Map map = mapper.readValue(dataString, Map.class);
            
            String result = map.get("result").toString();
            //스크립트 내용
            processResult.put("execSh", map.get("execSh").toString());
            //스크립트 결과
            processResult.put("result", result);
            log.info("스크립트 결과:\n{}", result);
        }
        
        //경로에 있는 로그파일 읽어서 Body로 보냄
        String path = "logs/" + logName + ".log";
        String str = Files.readString(Path.of(path));
        processResult.put("fileSize", exchange.getMessage().getHeader("Content-Length", String.class));
        processResult.put("logFile", str);
        jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(processResult);
        exchange.getMessage().setBody(jsonStr);
    }
}
