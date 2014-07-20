package kap.jackson.config;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

/**
 * Created by karl on 7/8/14.
 *
 * @deprecated not ready
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface Where {
    Qualifier[] value() default {};
    Class<?>[] prototype() default {};
}
