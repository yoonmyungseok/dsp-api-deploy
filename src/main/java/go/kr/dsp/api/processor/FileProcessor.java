package go.kr.dsp.api.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FileProcessor implements Processor {
  private final FluentProducerTemplate producerTemplate;

  @Override
  public void process(Exchange exchange) throws Exception {
    log.info("헤더: {}",exchange.getMessage().getHeaders());
    log.info("바디: {}",exchange.getMessage().getBody(String.class));
    String fileName = exchange.getMessage().getHeader("fileName", String.class);
    String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);
    String[] split=fileName.split("-");
    String inst=split[0];
    String service=split[1];
    String seq=split[2];
    String agent=split[3];
    /**
     * I0000001-S00001-1-IF-script-template.yaml
     * 기관코드-서비스코드-서버번호-에이전트
     */
    String request = producerTemplate
//      .withBody(exchange.getMessage().getBody())
//      .withHeader("serviceName",service)
//      .withHeader("fileExtension",fileExtension)
//      .withHeader("fileName",fileName)
//      .withHeader("agentName",agent)
      .toF("exec:sh?args="+fileName+"&workingDir="+agent)
      .request(String.class);
    log.info(request);
  }
}
