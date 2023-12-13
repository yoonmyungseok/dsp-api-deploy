package go.kr.dsp.api.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class ScriptProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String jsonStr = "";
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();
        
        log.info(System.getProperty("user.dir"));
        String execSh = exchange.getMessage().getBody(String.class);
        map.put("execSh", execSh);
        log.info("실행 스크립트: \n{}", execSh);
        String scriptPath = System.getProperty("user.dir") + "/script/" + exchange.getMessage().getHeader("fileName", String.class);
        
        //파일 권한
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwxrwx");
        Files.setPosixFilePermissions(Paths.get(scriptPath), perms);
        
        ProcessBuilder processBuilder = new ProcessBuilder(scriptPath);
        processBuilder.redirectErrorStream(true);
        
        try {
            Process process = processBuilder.start();
            StringBuilder str = new StringBuilder();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                    str.append(line).append("\n");
                }
            }
            map.put("result", str);
            log.info("결과 넣음");
            jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
            log.info("문자열로 바꿈");
            exchange.getMessage().setBody(jsonStr);
            log.info("바디에 넣음");
            
            int exitCode = process.waitFor();
            log.info("Exited with code " + exitCode);
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }
}
