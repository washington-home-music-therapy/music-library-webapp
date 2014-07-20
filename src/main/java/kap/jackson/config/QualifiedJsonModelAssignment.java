package kap.jackson.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.Annotation;

/**
 * Created by karl on 7/9/14.
 *
 * @deprecated not ready
 */
@Data
@Deprecated
public class QualifiedJsonModelAssignment {
    private Class<?> concrete;
    private Where where;
    private Class<?> jsonMixin;
    private int order;

    public boolean matches(Class<?> target) {
        if (!concrete.isAssignableFrom(target)) {
            return false;
        }

        return false;

    }
}
