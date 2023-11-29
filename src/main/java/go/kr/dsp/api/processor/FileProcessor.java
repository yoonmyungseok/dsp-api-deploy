package go.kr.dsp.api.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
public class FileProcessor implements Processor {
  @Override
  public void process(Exchange exchange) throws Exception {
    String fileName = exchange.getIn().getHeader("CamelFileName", String.class);
    String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);
    exchange.getMessage().setHeader("fileExtension",fileExtension);
  }
}
