package org.jboss.errai.forge.facet.resource;

import org.jboss.errai.forge.facet.plugin.WarPluginFacet;

/**
 * This facet creates a web.xml file if none exists in the project.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class WebXmlFacet extends AbstractFileResourceFacet {
  
  private final String templateFile = "template-web.xml";

  @Override
  protected String getResourceContent() throws Exception {
    return readResource(templateFile).toString();
  }

  @Override
  public String getRelFilePath() {
    return WarPluginFacet.getWarSourceDirectory(getProject()) + "/WEB-INF/web.xml";
  }

}
