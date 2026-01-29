package com.sk.skala.stockapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SKALA Stock API")
                        .version("1.0.0")
                        .description("주식 거래 시뮬레이션을 위한 REST API 문서입니다.\n\n"
                                + "이 API는 다음과 같은 기능을 제공합니다:\n"
                                + "- 주식 정보 관리 (조회, 등록, 수정, 삭제)\n"
                                + "- 플레이어 관리 (회원가입, 로그인, 정보 수정)\n"
                                + "- 주식 거래 (매수, 매도)")
                        .contact(new Contact()
                                .name("SK")
                                .email("contact@skala.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:9080")
                                .description("로컬 개발 서버")));
    }
}
