package com.ns.solve.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("solve-proejct")
                .description("hello world")
                .version("1.0.0");

        Server server = new Server();
        server.setUrl("https://ec2-3-34-134-27.ap-northeast-2.compute.amazonaws.com");

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));

    }


}
