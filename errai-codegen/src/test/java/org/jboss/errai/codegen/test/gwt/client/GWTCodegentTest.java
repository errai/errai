package org.jboss.errai.codegen.test.gwt.client;

import org.junit.Ignore;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * @author Mike Brock
 */
@Ignore
public class GWTCodegentTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.codegen.test.gwt.TypeOracleTests";
  }

  public void testGenerate() {
    GWT.create(TypeOracleBootstrap.class);
  }
}
