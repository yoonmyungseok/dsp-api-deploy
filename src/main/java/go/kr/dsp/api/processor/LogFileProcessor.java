package go.kr.dsp.api.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    Map<String, Object> map=new HashMap<>();

    if(exchange.getMessage().getHeader("fileExtension",String.class).equals("sh")){
      String request = exchange.getMessage().getBody(String.class);
      map.put("request",request);
      log.info("스크립트 결과:\n {}",request);
    }
    String path = "logs/"+logName+".log";
    String str=Files.readString(Path.of(path));
    map.put("fileSize",exchange.getMessage().getHeader("Content-Length",String.class));
    map.put("logFile",str);
    jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
    exchange.getMessage().setBody(jsonStr);
  }
}
