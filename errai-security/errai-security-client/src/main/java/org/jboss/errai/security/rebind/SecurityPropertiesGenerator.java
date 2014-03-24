package org.jboss.errai.security.rebind;

import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.config.rebind.AbstractAsyncGenerator;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.config.rebind.GenerateAsync;
import org.jboss.errai.security.client.local.context.SecurityProperties;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

@GenerateAsync(SecurityProperties.class)
public class SecurityPropertiesGenerator extends AbstractAsyncGenerator {

  public static final String ERRAI_USER_LOCAL_STORAGE = "errai.security.userLocalStorage";

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
            .get(ERRAI_USER_LOCAL_STORAGE);
    final boolean isLocalStorageAllowed;
    if (localStorageSetting == null || localStorageSetting.equals("false")) {
      isLocalStorageAllowed = false;
    }
    else if (localStorageSetting.equals("true")) {
      isLocalStorageAllowed = true;
    }
    else {
      throw new IllegalStateException("The ErraiApp property, " + ERRAI_USER_LOCAL_STORAGE
              + ", must have a value of \"true\" or \"false\". Given: " + localStorageSetting);
    }

    return isLocalStorageAllowed;
  }

}
