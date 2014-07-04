package kap.jackson.config;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import kap.springframework.config.AnnotationScanner;
import kap.springframework.config.AnnotationScannerBuilder;
import kap.springframework.config.AnnotationScannerJ8;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Uses AnnotationScanner to
 */
public class JsonConfigScanner {
    private static final AnnotationScannerBuilder<Class<?>> scanner =
            new AnnotationScannerBuilder<Class<?>>()
            .enableJava8Support()
            .map(JsonModel.class,JsonModel::value,a -> a.value() != null);

    public static Map<Class<?>, Collection<Class<?>>>
    collectModels(String basePackage) throws ClassNotFoundException {
        return scanner.build().toMap(basePackage);
    }
}
