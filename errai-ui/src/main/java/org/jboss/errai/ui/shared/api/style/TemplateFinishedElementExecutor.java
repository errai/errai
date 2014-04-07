package org.jboss.errai.ui.shared.api.style;

import com.google.gwt.dom.client.Element;
import java.lang.annotation.Annotation;

/**
 * ClassDescription for TemplateFinishedElementExecutor Interface to support the
 * Template Finished invoke.
 * 
 * @author Dennis Schumann <dennis.schumann@devbliss.com>
 */
public interface TemplateFinishedElementExecutor {

  /**
   * Invoke is called for every element that is annotated on a Templated class.
   * If the element has multiple times the same annotation the invoke method is
   * called for every annotation.
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
