package org.jboss.errai.client.framework.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface LoadTool {
    String name();
    String icon() default "";
    String group();
    int priority() default 0;
    boolean multipleAllowed() default false;
}
