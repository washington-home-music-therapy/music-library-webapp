package kap.springframework.config;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Provides classpath scanning for a subset of annotated types.
 *
 * @see org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
 */
public interface AnnotationScanner {

    void addType(Class<? extends Annotation> annotationClass);

    void addMetaType(Class<? extends Annotation> annotationClass);

    Set<Class<?>> scan(String basePackage);

    <K> Map<K, Class<?>> scanMap(String basePackage, final Function<Class<?>, K> keyMapper);

    <K> Map<K, Collection<Class<?>>>
    scanMultiMap(String basePackage,
                 Function<Class<?>, K> keyMapper);

    <T extends Annotation, K> Map<K, Collection<Class<?>>>
    scanType(String basePackage,
             Class<T> type,
             Function<T, K> keyMapper);

    <K> Map<K, Collection<Class<?>>>
    scanMultiType(String basePackage,
                  Map<Class<? extends Annotation>, Function<Annotation, K>> keyMappers,
                  Map<Class<? extends Annotation>, Predicate<Annotation>> includeFilter);
}
