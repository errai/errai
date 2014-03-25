package org.jboss.errai.forge.facet.resource;

import org.jboss.errai.forge.facet.plugin.WarPluginFacet;

public class ConfigXmlFacet extends AbstractFileResourceFacet {

  @Override
  protected String getResourceContent() throws Exception {
    return readResource(getRelFilePath()).toString();
  }

  @Override
  public String getRelFilePath() {
    return WarPluginFacet.getWarSourceDirectory(getProject()) + "/config.xml";
  }

}
