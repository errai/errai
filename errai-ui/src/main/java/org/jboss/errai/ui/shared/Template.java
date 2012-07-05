package org.jboss.errai.ui.shared;

import com.google.gwt.resources.client.TextResource;

/**
 * Interface for retrieving template contents.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface Template {

  /**
   * Get the template contents.
   */
  public TextResource getContents();
}
