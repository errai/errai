/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.forge.constant;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public final class ModuleVault {
  
  /**
   * An enumeration of GWT modules used by various facets.
   * 
   * @author Max Barkley <mbarkley@redhat.com>
   */
  public static enum Module {
    GwtUser("com.google.gwt.user.User"),
    ErraiCommon("org.jboss.errai.common.ErraiCommon"),
    ErraiBus("org.jboss.errai.bus.ErraiBus"),
    ErraiIoc("org.jboss.errai.ioc.Container"),
    ErraiCdi("org.jboss.errai.enterprise.CDI"),
    ErraiUi("org.jboss.errai.ui.UI"),
    ErraiNavigation("org.jboss.errai.ui.nav.Navigation"),
    ErraiDataBinding("org.jboss.errai.databinding.DataBinding"),
    ErraiJpa("org.jboss.errai.jpa.JPA"),
    ErraiJpaDatasync("org.jboss.errai.jpa.sync.DataSync"),
    ErraiJaxrs("org.jboss.errai.enterprise.Jaxrs"),
    ErraiSecurity("org.jboss.errai.security.Security"),
    ErraiCordova("org.jboss.errai.ui.Cordova");
    
    private final String logicalName;
    private Module(final String logicalName) {
      this.logicalName = logicalName;
    }
    
    /**
     * @return The fully qualified logical name of this GWT module.
     */
    public String getLogicalName() {
      return logicalName;
    }
  }

}
