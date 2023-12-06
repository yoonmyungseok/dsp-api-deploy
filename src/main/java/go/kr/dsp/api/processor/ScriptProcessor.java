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
public class ScriptProcessor implements Processor {
  private final FluentProducerTemplate producerTemplate;

  @Override
  public void process(Exchange exchange) throws Exception {
    String fileName = exchange.getMessage().getHeader("CamelFileName", String.class);
    String[] split=fileName.split("-");
    String inst=split[0];
    String service=split[1];
    String seq=split[2];
    String agent=split[3];
    String request = producerTemplate.withBody(exchange.getMessage().getBody())
      .withHeaders(exchange.getMessage().getHeaders())
      .toF("exec:sh?args="+fileName+"&workingDir="+agent)
      .request(String.class);
    exchange.getMessage().setBody(request);
    log.info(exchange.getMessage().getBody(String.class));
    log.info(exchange.getMessage().getHeaders().toString());
  }
}
