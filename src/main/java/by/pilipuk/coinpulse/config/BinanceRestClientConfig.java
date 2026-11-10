package by.pilipuk.coinpulse.config;

import by.pilipuk.coinpulse.client.property.BinanceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
@EnableConfigurationProperties(BinanceProperties.class)
public class BinanceRestClientConfig {

    @Bean
    public HttpClient binanceHttpClient(BinanceProperties properties) {
        return HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();
    }

    @Bean
    public RestClient binanceRestClient(
            RestClient.Builder builder, BinanceProperties properties, HttpClient binanceHttpClient
    ) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(binanceHttpClient);

        requestFactory.setReadTimeout(properties.readTimeout());

        return builder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}
