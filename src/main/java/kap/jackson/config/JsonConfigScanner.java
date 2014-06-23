package kap.jackson.config;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import kap.springframework.config.AnnotationScanner;
import kap.springframework.config.AnnotationScannerBuilder;
import kap.springframework.config.AnnotationScannerJ8;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Uses AnnotationScanner to
 */
public class JsonConfigScanner {
    private static final AnnotationScanner DEFAULT = new AnnotationScannerJ8();

    public static Map<Class<?>, Collection<Class<?>>>
    collectModels(String basePackage) {
//        return DEFAULT.scanType(basePackage,JsonModel.class,JsonModel::value);
        return DEFAULT.scanMultiType(
                basePackage,
                map(JsonModel.class,JsonModel::value),
                filter(JsonModel.class,a -> a.value() != null));
    }

    private static <T extends Annotation,K> Map<Class<? extends Annotation>, Function<Annotation, K>>
    map(Class<T> aClass, Function<T,K> instanceFunction) {

        Map<Class<? extends Annotation>, Function<Annotation, K>> m = new IdentityHashMap<>();
        m.put(aClass,(Function<Annotation,K>)instanceFunction);
        return m;
    }

    private static <T extends Annotation> Map<Class<? extends Annotation>, Predicate<Annotation>>
    filter(Class<T> aClass, Predicate<T> instanceFunction) {
        return null;
    }
}
