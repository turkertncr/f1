package org.app.f1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient openF1RestClient(RestClient.Builder builder, @Value("${openf1.base-url}") String base) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build());
        requestFactory.setReadTimeout(Duration.ofSeconds(30));

        return builder
                .baseUrl(base)
                .requestFactory(requestFactory)
                .build();
    }
}
