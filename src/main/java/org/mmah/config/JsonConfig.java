package org.mmah.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kap.jackson.config.JsonConfigScanner;
import org.springframework.context.annotation.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.*;

/**
 * Created by karl on 4/24/14.
 */
@Configuration
public class JsonConfig {

    @Bean
    public Map<Class<?>, Collection<Class<?>>> mixInAnnotations() {
        return JsonConfigScanner.collectModels("org.mmah.model.json");
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mixInAnnotations().forEach((key, values) ->
                values.forEach(it ->
                        mapper.addMixInAnnotations(key, it)
                )
        );
        return mapper;
    }

    @Bean
    public MappingJackson2HttpMessageConverter jsonMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }
}
