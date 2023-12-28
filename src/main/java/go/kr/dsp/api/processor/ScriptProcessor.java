package go.kr.dsp.api.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
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
        
        log.info("디렉토리: {}", System.getProperty("user.dir"));
        //Body에 담은 스크립트
        String execSh = exchange.getMessage().getBody(String.class);
        map.put("execSh", execSh);
        log.info("실행 스크립트: \n{}", execSh);
        //실행할 스크립트 경로
        String scriptPath = System.getProperty("user.dir") + "/script/" + exchange.getMessage().getHeader("fileName", String.class);
        
        //파일 권한
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwxrwx");
        Files.setPosixFilePermissions(Paths.get(scriptPath), perms);
        
        //스크립트 실행 프로세스
        ProcessBuilder processBuilder = new ProcessBuilder(scriptPath);
        processBuilder.redirectErrorStream(true);
        
        try {
            Process process = processBuilder.start();
            StringBuilder str = new StringBuilder();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = "";
                while ((line = reader.readLine()) != null) {
                    str.append(line).append("\n");
                    log.info(line);
                }
            }
            map.put("result", str);
            jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
            exchange.getMessage().setBody(jsonStr);
            
            int exitCode = process.waitFor();
            log.info("Exited with code " + exitCode);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
}
