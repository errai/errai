package org.jboss.errai.ui.shared.api.style;

import com.google.gwt.dom.client.Element;
import java.lang.annotation.Annotation;

/**
 * ClassDescription for TemplateFinishedElementExecutor
 *
 * @author Dennis Schumann <dennis.schumann@devbliss.com>
 */
public interface TemplateFinishedElementExecutor {
  public void invoke(Element element, Annotation annoation);
}
