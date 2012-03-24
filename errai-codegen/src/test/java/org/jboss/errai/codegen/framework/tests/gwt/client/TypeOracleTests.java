package org.jboss.errai.codegen.framework.tests.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;

/**
 * @author Mike Brock
 */
public class TypeOracleTests implements EntryPoint {
  public void onModuleLoad() {
    GWT.create(TypeOracleBootstrap.class);
  }
}
