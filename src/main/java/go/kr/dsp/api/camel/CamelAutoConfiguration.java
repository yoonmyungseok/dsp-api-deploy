package go.kr.dsp.api.camel;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
public class CamelAutoConfiguration {
    
    private final AgentRouteBuilder agentRouteBuilder;
    
    public CamelAutoConfiguration(AgentRouteBuilder agentRouteBuilder) {
        this.agentRouteBuilder = agentRouteBuilder;
    }
    
    @Bean
    CamelContextConfiguration contextConfiguration() throws Exception {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext camelContext) {
                System.setProperty("route.from", agentRouteBuilder.build());
            }
            
            @Override
            public void afterApplicationStart(CamelContext camelContext) {
            
            }
        };
    }
    
}
