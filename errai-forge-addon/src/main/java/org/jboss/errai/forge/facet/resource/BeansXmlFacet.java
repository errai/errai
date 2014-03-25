package org.jboss.errai.forge.facet.resource;

import org.jboss.errai.forge.facet.plugin.WarPluginFacet;

public class BeansXmlFacet extends AbstractFileResourceFacet {
  
  private final String templateResource = "template-beans.xml";

  @Override
  protected String getResourceContent() throws Exception {
    return readResource(templateResource).toString();
  }

  @Override
  public String getRelFilePath() {
    return WarPluginFacet.getWarSourceDirectory(getProject()) + "/WEB-INF/beans.xml";
  }

}
