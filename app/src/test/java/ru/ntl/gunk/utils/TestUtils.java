package ru.ntl.gunk.utils;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

public class TestUtils {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();


    public static RestTemplate buildTemplate(String url){
        var restTemplate = new RestTemplateBuilder()
                .rootUri(url)
                .errorHandler(TestUtils.errorSkip())
                .build();
        var factory = new SimpleClientHttpRequestFactory();
        factory.setOutputStreaming(false);
        restTemplate.setRequestFactory(factory);
        return restTemplate;
    }

    private static ResponseErrorHandler errorSkip() {
        return new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) {
                System.out.println(response);
            }
        };
    }

    public static String encryptedPassword(String pass){
        return PASSWORD_ENCODER.encode(pass);
    }
}
