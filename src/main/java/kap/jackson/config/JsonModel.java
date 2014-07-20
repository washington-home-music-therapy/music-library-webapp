package kap.jackson.config;

import org.springframework.core.annotation.Order;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a concrete class that the annotated class which defines Jackson Mix-In annotations.
 *
 * Specify "orElse" or use @Order on the annotated class to specify the order in which @where conditions
 * are evaluated (If both strategies are mixed then types declaring @Order are sorted, "orElse" children
 * are evaluated and inserted into the list where order has been specified (excluding duplicates). Then
 * finally all models which remain orphaned are prepended to the ruleset. This means that "orElse" may
 * drastically alter a model's evaluation priority, tho it is more expressive.)
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonModel {
    /**
     * The concrete class which will benefit from the annotated class's type and field annotations
     */
    Class<?> value();

    /**
     * A set of @Qualifiers and meta-annotation prototypes to limit the assignment of this mix-in.
     * <p>
     * For example a class annotated with @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')" would restrict
     *
     * @deprecated not ready
     */
    @Deprecated
    Where where() default @Where;

    /**
     * The model to evaluate next for the given concrete type if the where clause is not satisfied.
     *
     * @deprecated not ready
     */
    @Deprecated
    Class<?> orElse() default Object.class;
}
