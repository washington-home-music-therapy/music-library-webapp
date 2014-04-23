package org.mmah.itunes;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import java.util.IdentityHashMap;
import java.util.Set;

@SupportedAnnotationTypes({"*"}) // TODO limit to json annotations
public class AnnotationProcessor extends AbstractProcessor {
    private static final IdentityHashMap<Class<?>,Class<?>> discoveredClasses = new IdentityHashMap<Class<?>, Class<?>>();
    public static Set<Class<?>> getDiscoveredClasses() {
        return null;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // example: lombok.core.AnnotationProcessor
        return false;
    }
}
