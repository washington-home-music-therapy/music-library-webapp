package kap.jackson.config;

import kap.springframework.config.AnnotationScannerBuilder;

import java.util.Collection;
import java.util.Map;

/**
 * Uses AnnotationScanner to
 */
public class JsonConfigScanner {
    private static final AnnotationScannerBuilder<Class<?>> scanner =
            new AnnotationScannerBuilder<Class<?>>()
            .enableJava8Support()
            .map(JsonModel.class,JsonModel::value,a -> a.value() != null);

//    private static final AnnotationScannerBuilder<QualifiedJsonModelAssignment> qualifiedScanner =
//            new AnnotationScannerBuilder<QualifiedJsonModelAssignment>()
//            .enableJava8Support()
//            .enableMetaAnnotation()
//            .map(JsonModel.class, it -> it.)
//
    public static Map<Class<?>, Collection<Class<?>>>
    collectModels(String basePackage) {
        return scanner.build().toMap(basePackage);
    }
}
