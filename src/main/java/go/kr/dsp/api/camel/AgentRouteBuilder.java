package go.kr.dsp.api.camel;

import go.kr.dsp.api.util.apiInfo.ApiInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static go.kr.dsp.api.util.agentdata.AgentApiInfoService.agentApiInfoMemory;

@Slf4j
@Component
public class AgentRouteBuilder {

    @Value("${camel.rest.file-path}")
    private String dir;

    @Value("${data.domain}")
    private String domain;
    @SneakyThrows
    public void build(CamelContext camelContext) {
        SpringCamelContext springCamelContext = (SpringCamelContext) camelContext;
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            stream
                    .filter(file -> !Files.isDirectory(file))
                    .filter(file -> file.getFileName().toString().equals("external-template.yaml"))
                    .forEach(path -> {
                        Yaml yaml = new Yaml();
                        try (InputStream inputStream = Files.newInputStream(path)) {
                            List<Map<String, Object>> list = yaml.load(inputStream);
                            for(Map<String, Object> map: list) {
                                Map<String, Object> innerMap = (Map<String, Object>) map.get("route-template");
                                String routeTemplateRef = (String) innerMap.get("id");
                                for(ApiInfo apiInfo : agentApiInfoMemory) {
                                    if(apiInfo.getRevYN().equals("N")) {
                                        try {
                                            String url = apiInfo.getApiUrl();
                                            Pattern pattern = Pattern.compile(domain + "/api/([A-Z0-9]+)/([a-zA-Z0-9]+)(/[a-zA-Z0-9]+)?");
                                            Matcher matcher = pattern.matcher(url);
                                            if (matcher.find()) {
                                                String InstId = matcher.group(1);
                                                String apiGroupName = matcher.group(2);
                                                String apiName = matcher.group(3).substring(1);
                                                String routeId = InstId + "-" + apiGroupName + "-" + apiName;
                                                Map<String, Object> parameters = new HashMap<>();
                                                parameters.put("RestPath", "/" + InstId + "/" + apiGroupName + "/" + apiName);
                                                String URL = apiInfo.getResponseUrl().replace("//", "");
                                                log.info("######### URL ######### : " + URL);
                                                parameters.put("URL", URL);
                                                springCamelContext.addRouteFromTemplate(routeId, routeTemplateRef, parameters);
                                            }
                                        } catch (Exception e) {
                                            log.info(apiInfo.getApiUrl());
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        log.info("Running CamelContext: " + springCamelContext.getName());
    }
}
