package org.mmah.config;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * Date: 12/2/13
 * Time: 4:15 PM
 * To change this template use File | Settings | File Templates.
 */
@Configuration
public class LibraryConfig {
    @Bean
    public File dataDir() {
        File dir = new File("data").getAbsoluteFile();
        if((dir.exists() && dir.isDirectory()) || dir.mkdirs()) {
            return dir;
        }
        throw new IllegalStateException("can't create directory: " + dir);
    }

    @Bean
    public Random random() {
        SecureRandom sr =  new SecureRandom();
        sr.setSeed(sr.generateSeed(8));
        return sr;
    }
}
