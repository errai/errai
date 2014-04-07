package org.jboss.errai.ui.test.template.finished.client.res;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jboss.errai.ui.shared.api.annotations.style.TemplateFinishedBinding;

/**
 * 
 * @author Dennis Schumann <dennis.schumann@devbliss.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@TemplateFinishedBinding
public @interface PermissionAnnotation {
  String value();
}
