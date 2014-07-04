package kap.springframework.config;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import kap.springframework.config.AnnotationScanner;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Java 7 / Guava annotation scanner impl. Fail-fast classloading.
 */
public class AnnotationScannerJ7 extends ClassPathScanningCandidateComponentProvider implements AnnotationScanner {

    public AnnotationScannerJ7() {
        super(false);
    }

    @Override
    public void addType(Class<? extends Annotation> annotationClass) {
        addIncludeFilter(new AnnotationTypeFilter(annotationClass, false));
    }

    @Override
    public void addMetaType(Class<? extends Annotation> annotationClass) {
        addIncludeFilter(new AnnotationTypeFilter(annotationClass,true));
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return true;
    }

    protected Collection<Class<?>> findClasses(final String basePackage) {
        Set<BeanDefinition> beanDefinitionSet = findCandidateComponents(basePackage);

        return Collections2.transform(beanDefinitionSet,
                new Function<BeanDefinition, Class<?>>() {
                    @Override
                    public Class<?> apply(BeanDefinition beanDefinition) {
                        try {
                            return Class.forName(beanDefinition.getBeanClassName());
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(
                                    "scanning " + basePackage + " inside " + beanDefinition.getBeanClassName(),
                                    e);
                        }
                    }
                }
        );
    }

    public Set<Class<?>> scan(String basePackage) {
        Set<Class<?>> result = Sets.newIdentityHashSet();
        result.addAll(findClasses(basePackage));
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K> Map<K, Class<?>> scanMap(String basePackage, final Function<Class<?>, K> keyMapper) {

        return Maps.uniqueIndex(findClasses(basePackage), (Function)keyMapper);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K> Map<K, Collection<Class<?>>>
    scanMultiMap(String basePackage,
                 Function<Class<?>, K> keyMapper) {

        return Multimaps.<K,Class<?>>index(findClasses(basePackage),(Function)keyMapper).asMap();
    }

    @Override
    public <T extends Annotation, K> Map<K, Collection<Class<?>>>
    scanType(String basePackage,
             Class<T> type,
             Function<T, K> keyMapper) {

        Function<Class<?>,K> transform = Functions.compose(keyMapper,new Function<Class<?>, T>() {
            @Override
            public T apply(Class<?> o) {
                return o.getAnnotation(type);
            }
        });

        return Multimaps.index(findClasses(basePackage),transform).asMap();
    }

    @Override
    public <K> Map<K, Collection<Class<?>>>
    scanMultiType(String basePackage,
                  Map<Class<? extends Annotation>, Function<Annotation, K>> keyMappers,
                  Map<Class<? extends Annotation>, Predicate<Annotation>> includeFilter) {

        return index(findClasses(basePackage).iterator(), new Function<Class<?>, Collection<K>>() {
            @Override
            public Collection<K> apply(final Class<?> aClass) {
                // Annotation -> annotation type map for this class
                List<K> result = new ArrayList<K>();
                for (Class<? extends Annotation> type : keyMappers.keySet()) {
                    for (Annotation a : aClass.getAnnotationsByType(type)) {
                        // filter ignored
                        if(!includeFilter.get(type).apply(a)) continue;
                        // apply mapper function per annotation
                        result.add(keyMappers.get(type).apply(a));
                    }
                }
                return result;
            }
        }).asMap();
    }

    // this code is copied from Guava 18.0
    public static <K, V> ImmutableListMultimap<K, V> index(
            Iterator<V> values, Function<? super V, ? extends Iterable<K>> keyFunction) {
        checkNotNull(keyFunction);
        ImmutableListMultimap.Builder<K, V> builder
                = ImmutableListMultimap.builder();
        while (values.hasNext()) {
            V value = values.next();
            checkNotNull(value, values);
            // Guava does not provide for multiple key mapping results
            for (K key : keyFunction.apply(value)) {
                builder.put(key, value);
            }
        }
        return builder.build();
    }
}
