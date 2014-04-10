package org.jboss.errai.ui.shared.api.annotations.style;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jboss.errai.ui.shared.api.style.TemplateFinishedRegistry;

/**
 * Annotation indicates other Annotations on UI Fields. That Annotations should
 * have executors registered in {@link TemplateFinishedRegistry}.
 * 
 * @author Dennis Schumann <dennis.schumann@devbliss.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface TemplateFinishedBinding {
}
