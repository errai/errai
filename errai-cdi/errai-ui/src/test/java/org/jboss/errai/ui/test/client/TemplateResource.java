package org.jboss.errai.ui.test.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface TemplateResource extends ClientBundle {
  public static final TemplateResource INSTANCE = GWT
          .create(TemplateResource.class);

  @Source("template.html")
  public TextResource getTemplate();
}