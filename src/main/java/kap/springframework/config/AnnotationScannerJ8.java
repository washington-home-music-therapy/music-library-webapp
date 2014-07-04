package kap.springframework.config;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Java 8 lambda/stream annotation scanner impl. Logs and continues when encountering classloading errors.
 */
public class AnnotationScannerJ8 extends ClassPathScanningCandidateComponentProvider implements AnnotationScanner {

    public AnnotationScannerJ8() {
        super(false);
    }

    @Override
    public void addType(Class<? extends Annotation> annotationClass) {
        addIncludeFilter(new AnnotationTypeFilter(annotationClass,false));
    }

    @Override
    public void addMetaType(Class<? extends Annotation> annotationClass) {
        addIncludeFilter(new AnnotationTypeFilter(annotationClass, true));
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return true;
    }

    protected Stream<Class<?>> stream(String basePackage) {
        Set<BeanDefinition> beanDefinitionSet = findCandidateComponents(basePackage);
        return beanDefinitionSet.stream()
                .map(BeanDefinition::getBeanClassName)
                .flatMap(AnnotationScannerJ8::classesForName)
                .distinct();
    }

    protected static Stream<Class<?>> classesForName(String name) {
        try {
            return Stream.of(Class.forName(name));
        } catch (ClassNotFoundException e) {
            LoggerFactory.getLogger(AnnotationScannerJ8.class)
                    .error("inside {}", name, e);

            return Stream.empty();
        }
    }

    @Override
    public Set<Class<?>> scan(String basePackage) {
        return stream(basePackage).collect(Collectors.toSet());
    }

    @Override
    public <K> Map<K, Class<?>>
    scanMap(String basePackage, final Function<Class<?>, K> keyMapper) {

        return stream(basePackage).collect(Collectors.toMap(
                keyMapper::apply,
                java.util.function.Function.identity()));
    }

    @Override
    public <K> Map<K, Collection<Class<?>>>
    scanMultiMap(String basePackage,
                 Function<Class<?>, K> keyMapper) {

        return stream(basePackage)
                .collect(Collectors.<Class<?>, K, Collection<Class<?>>>toMap(
                        keyMapper::apply,
                        aClass -> { // value function
                            Set<Class<?>> s = Sets.newIdentityHashSet();
                            s.add(aClass);
                            return s;
                        },
                        (left, right) -> { // value reducer function
                            left.addAll(right);
                            return left;
                        }
                )
                );
    }

    @Override
    public <T extends Annotation, K> Map<K, Collection<Class<?>>>
    scanType(String basePackage,
             Class<T> type,
             Function<T, K> keyMapper) {

        return stream(basePackage)
                .filter(aClass -> aClass.getAnnotation(type) != null)
                .collect(Collectors.toMap( // <Class<?>,K,Collection<Class<?>>>
                        aClass -> keyMapper.apply(aClass.getAnnotation(type)),
                        aClass -> { // value function
                            Set<Class<?>> s = Sets.newIdentityHashSet();
                            s.add(aClass);
                            return s;
                        },
                        (left, right) -> { // value reducer function
                            left.addAll(right);
                            return left;
                        }
                )
                );
    }

    @Override
    public <K> Map<K, Collection<Class<?>>>
    scanMultiType(String basePackage,
                  Map<Class<? extends Annotation>, Function<Annotation, K>> keyMappers,
                  Map<Class<? extends Annotation>, Predicate<Annotation>> includeFilter) {

        return stream(basePackage)
                .flatMap(aClass -> findAnnotations(aClass, keyMappers.keySet()).entrySet().stream()
                        .filter(entry -> {
                            Annotation a = entry.getKey();
                            return includeFilter.get(a.annotationType()).apply(a);
                        }))
                .collect(Collectors.toMap( // <Map.Entry<...>,Class<?>,Collection<Class<?>>>
                        entry -> { // key function
                            Annotation a = entry.getKey();
                            return keyMappers.get(a.annotationType()).apply(a);
                        },
                        entry -> { // value function
                            Set<Class<?>> s = Sets.newIdentityHashSet();
                            s.add(entry.getValue());
                            return s;
                        },
                        (left, right) -> { // value reducer function
                            left.addAll(right);
                            return left;
                        }
                ));
    }

    private Map<Annotation, Class<?>> findAnnotations(Class<?> component, Set<Class<? extends Annotation>> candidates) {
        IdentityHashMap<Annotation, Class<?>> result = new IdentityHashMap<>();
        for (Class<? extends Annotation> it : candidates) {
            for (Annotation a : component.getAnnotationsByType(it)) {
                result.put(a, component);
            }
        }
        return result;
    }
}
