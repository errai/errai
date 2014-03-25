package org.jboss.errai.forge.facet.resource;

import org.jboss.errai.forge.facet.base.CoreBuildFacet;


public class ErraiAppPropertiesFacet extends AbstractFileResourceFacet {

  @Override
  protected String getResourceContent() throws Exception {
    return "";
  }

  @Override
  public String getRelFilePath() {
    return CoreBuildFacet.getResourceDirectory(getProject()) + "/ErraiApp.properties";
  }
  
}
