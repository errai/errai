package org.jboss.errai.codegen.tests.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

/**
 * @author Mike Brock
 */
public class TypeOracleTests implements EntryPoint {
  public void onModuleLoad() {
    GWT.create(TypeOracleBootstrap.class);
  }
}
