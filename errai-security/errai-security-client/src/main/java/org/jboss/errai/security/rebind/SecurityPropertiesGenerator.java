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

package org.jboss.errai.security.rebind;

import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.config.rebind.AbstractAsyncGenerator;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.config.rebind.GenerateAsync;
import org.jboss.errai.security.Properties;
import org.jboss.errai.security.client.local.storage.SecurityProperties;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

@GenerateAsync(SecurityProperties.class)
public class SecurityPropertiesGenerator extends AbstractAsyncGenerator {

  public static final String USER_COOKIE_ENABLED_PROP = Properties.USER_COOKIE_ENABLED;

  private static final String PACKAGE_NAME = SecurityProperties.class.getPackage().getName();
  private static final String CLASS_NAME = SecurityProperties.class.getSimpleName() + "Impl";
  private static final String LOCAL_STORAGE_METHOD_NAME = "isLocalStorageOfUserAllowed";

  @Override
  protected String generate(final TreeLogger logger, final GeneratorContext context) {
    final boolean isLocalStorageAllowed = isLocalStorageSettingEnabled();

    return ClassBuilder.define(PACKAGE_NAME + "." + CLASS_NAME)
            .publicScope().implementsInterface(SecurityProperties.class)
            .body()
            .publicMethod(Boolean.class, LOCAL_STORAGE_METHOD_NAME)
              .body()
              .append(Stmt.loadLiteral(isLocalStorageAllowed).returnValue())
              .finish()
            .toJavaString();
  }

  @Override
  public String generate(final TreeLogger logger, final GeneratorContext context, final String typeName)
          throws UnableToCompleteException {
    return startAsyncGeneratorsAndWaitFor(SecurityProperties.class, context, logger, PACKAGE_NAME, CLASS_NAME);
  }

  private boolean isLocalStorageSettingEnabled() {
    final String localStorageSetting = EnvUtil.getEnvironmentConfig().getFrameworkProperties()
            .get(USER_COOKIE_ENABLED_PROP);
    final boolean isLocalStorageAllowed;
    if (localStorageSetting == null || localStorageSetting.equals("false")) {
      isLocalStorageAllowed = false;
    }
    else if (localStorageSetting.equals("true")) {
      isLocalStorageAllowed = true;
    }
    else {
      throw new IllegalStateException("The ErraiApp property, " + USER_COOKIE_ENABLED_PROP
              + ", must have a value of \"true\" or \"false\". Given: " + localStorageSetting);
    }

    return isLocalStorageAllowed;
  }

  @Override
  protected boolean isCacheValid() {
    return false;
  }

}
