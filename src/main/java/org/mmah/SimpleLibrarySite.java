package org.mmah;

import org.mmah.config.JsonConfig;
import org.mmah.config.LibraryConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@Controller
@EnableAutoConfiguration
@Import({
        LibraryConfig.class,
        JsonConfig.class,
})
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
}
