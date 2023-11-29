package go.kr.dsp.api.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RequestProcessor implements Processor {

  @Override
  public void process(Exchange exchange) throws Exception {
    String[] split = exchange.getMessage().getHeader(Exchange.HTTP_PATH, String.class).split("/");
    exchange.getMessage().setHeader("httpPath",split[1]);
    exchange.getMessage().setHeader("funcPath",split[2]);
  }
}
