package kap.springframework.config;

/**
 * Created by karl on 6/22/14.
 */
public class AnnotationScannerBuilder {

    private Class<? extends AnnotationScanner> factory = AnnotationScannerJ7.class;

    private boolean metaAnnotation;

    public AnnotationScannerBuilder enableMetaAnnotation() {
        metaAnnotation = true;
        return this;
    }

    @SuppressWarnings("unchecked")
    public AnnotationScannerBuilder enableJava8Support() throws ClassNotFoundException {
        factory = (Class<? extends AnnotationScanner>)Class.forName("kap.springframework.config.AnnotationScannerJ8");
        return this;
    }

    public AnnotationScannerBuilder map() {
        return this;
    }

    public AnnotationScanTask build() {
        try {
            AnnotationScanTask instance = new AnnotationScanTask();
            instance.scanner = factory.newInstance();

            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static class AnnotationScanTask {
        AnnotationScanner scanner;

    }
}
