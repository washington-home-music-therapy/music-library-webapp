package kap.springframework.config;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Fluent builder interface to annotation scanner. Eliminates repetition in adding class scanning.
 */
public class AnnotationScannerBuilder<K> {

    private Class<? extends AnnotationScanner> factory = AnnotationScannerJ7.class;
    private Map<Class<? extends Annotation>, Function<Annotation,K>> keyFunctions = new HashMap<>();
    private Map<Class<? extends Annotation>, Predicate<Annotation>> predicateFunctions = new HashMap<>();

    private boolean metaAnnotation;

    private Map<Class<? extends Annotation>, Function<Annotation, K>> map = new HashMap<>();

    public AnnotationScannerBuilder<K> enableMetaAnnotation() {
        metaAnnotation = true;
        return this;
    }

    @SuppressWarnings("unchecked")
    public AnnotationScannerBuilder<K>
    enableJava8Support() {
        try {
            factory = (Class<? extends AnnotationScanner>)Class.forName("kap.springframework.config.AnnotationScannerJ8");
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <A extends Annotation> AnnotationScannerBuilder<K>
    map(Class<A> aClass, Function<A,? extends K> instanceFunction) {
        keyFunctions.put(aClass, (Function<Annotation, K>) instanceFunction);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <A extends Annotation> AnnotationScannerBuilder<K>
    map(Class<A> aClass, Function<A,? extends K> keyFunction, Predicate<A> predicateFunction) {
        keyFunctions.put(aClass, (Function<Annotation, K>) keyFunction);
        predicateFunctions.put(aClass,(Predicate<Annotation>)predicateFunction);
        return this;
    }

    public AnnotationScanTask<K> build() {
        try {
            AnnotationScanTask<K> instance = new AnnotationScanTask<K>();
            instance.scanner = factory.newInstance();
            instance.keys = keyFunctions;
            instance.predicates = predicateFunctions;

            Set<Class<? extends Annotation>> classes = Sets.newIdentityHashSet();
            classes.addAll(keyFunctions.keySet());
            classes.addAll(predicateFunctions.keySet());

            for(Class<? extends Annotation> aClass : classes) {
                if(metaAnnotation) {
                    instance.scanner.addMetaType(aClass);
                } else {
                    instance.scanner.addType(aClass);
                }
            }

            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static class AnnotationScanTask<R> {
        private AnnotationScanner scanner;
        private Map<Class<? extends Annotation>, Function<Annotation,R>> keys = new HashMap<>();
        private Map<Class<? extends Annotation>, Predicate<Annotation>> predicates = new HashMap<>();

        public Map<R,Collection<Class<?>>> toMap(String basePackage) {
            return scanner.scanMultiType(basePackage, keys, predicates);
        }
    }
}
