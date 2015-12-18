package org.jboss.errai.ui.test.runtime.client.res;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ui.client.local.spi.TemplateProvider;
import org.jboss.errai.ui.client.local.spi.TemplateRenderingCallback;

@Dependent
public class CustomTemplateProvider implements TemplateProvider {

  @Override
  public void provideTemplate(String location, TemplateRenderingCallback renderingCallback) {
    String template = 
            "<div id=\"root\">"
            + "<h1>This will be rendered</h1>"
            + "<div id=\"c1\" class=\"c1\" align=\"left\">This will not be rendered</div>"
            + "<div id=\"c2\">This will be rendered inside button</div>"
            + "<div>This will be rendered</div>"
            + "<input id=\"c3\" name=\"address\" /> "
            + "<a id=\"c4\" href=\"blah\"><span>LinkHTML</span></a> "
            + "<a href=\"blah2\" id=\"c5\"><img id=\"c6\" src=\"/some/img.png\"/></a>"
            + "</div>";
    
    renderingCallback.renderTemplate(template);
  }

}
