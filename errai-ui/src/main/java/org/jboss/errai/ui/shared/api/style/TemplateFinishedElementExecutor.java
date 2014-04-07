package org.jboss.errai.ui.shared.api.style;

import com.google.gwt.dom.client.Element;
import java.lang.annotation.Annotation;

/**
 * ClassDescription for TemplateFinishedElementExecutor Interface to support the Template Finished invoke.
 * This executors need to be registered at the {@link TemplateFinishedRegistry}
 * It's possible to have multiple executors for the same Annotation.
 * 
 * 
 * @author Dennis Schumann <dennis.schumann@devbliss.com>
 */
public interface TemplateFinishedElementExecutor {

  /**
   * Invoke is called for every element that is annotated on a Templated class.
   * 
   * @param element
   *          The dom element that is annotated
   * @param annoation
   *          The element's annotation
   */
  public void invoke(Element element, Annotation annoation);

  /**
   * Return the supported annotation type here.
   * 
   * @return supported annotation type
   */
  public Class<? extends Annotation> getTargetAnnotationType();
}
