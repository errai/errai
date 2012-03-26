package org.jboss.errai.codegen.framework.tests.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * @author Mike Brock
 */
public class GWTCodegentTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.codegen.framework.tests.gwt.TypeOracleTests";
  }

  public void testGenerate() {
    GWT.create(TypeOracleBootstrap.class);
  }
}
