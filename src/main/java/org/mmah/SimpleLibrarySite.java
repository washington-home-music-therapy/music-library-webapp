package org.mmah;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@Controller
@ComponentScan
@EnableAutoConfiguration
public class SimpleLibrarySite {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(SimpleLibrarySite.class, args);
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setMaxUploadSize(100 * 1024 * 1024);
        return resolver;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // TODO discover mixin types via annotations (componentscan? aop?)
//        mapper.addMixInAnnotations();
        return mapper;
    }

    @Bean
    public MappingJackson2HttpMessageConverter jsonMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }
}
