package org.mmah;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * Created with IntelliJ IDEA.
 * Date: 11/16/13
 * Time: 3:51 PM
 * To change this template use File | Settings | File Templates.
 */
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
}
