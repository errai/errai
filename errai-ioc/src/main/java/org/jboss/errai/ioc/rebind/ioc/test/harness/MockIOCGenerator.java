/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.rebind.ioc.test.harness;


import com.google.gwt.core.ext.TreeLogger;
import org.jboss.errai.codegen.util.ClassChangeUtil;
import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.ioc.client.Bootstrapper;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCBootstrapGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Set;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MockIOCGenerator {

  private Set<String> packages;

  public MockIOCGenerator(Set<String> packages) {
    this.packages = Assert.notNull(packages);
  }

  public Class<? extends Bootstrapper> generate() {
    String packageName = Bootstrapper.class.getPackage().getName();
    String className = "MockBootstrapperImpl";

    final IOCBootstrapGenerator bootstrapGenerator = new IOCBootstrapGenerator(null,
            new TreeLogger() {
                  @Override
                  public TreeLogger branch(Type type, String msg, Throwable caught, HelpInfo helpInfo) {
                    return null;
                  }

                  @Override
                  public boolean isLoggable(Type type) {
                    return false;
                  }

                  @Override
                  public void log(Type type, String msg, Throwable caught, HelpInfo helpInfo) {
                    System.out.println(type.getLabel() + ": " + msg);
                    if (caught != null) {
                      caught.printStackTrace();
                    }
                  }
                }, packages, true);


    final String classStr = bootstrapGenerator.generate(packageName, className);

    try {
      File directory =
              new File(RebindUtils.getTempDirectory() + "/ioc/classes/" + packageName.replaceAll("\\.", "/"));

      File sourceFile = new File(directory.getAbsolutePath() + "/" + className + ".java");
      File outFile = new File(directory.getAbsolutePath() + "/" + className + ".class");

      if (sourceFile.exists()) {
        sourceFile.delete();
        outFile.delete();
      }

      directory.mkdirs();

      FileOutputStream outputStream = new FileOutputStream(sourceFile);

      outputStream.write(classStr.getBytes("UTF-8"));
      outputStream.flush();
      outputStream.close();

      System.out.println("wrote file: " + sourceFile.getAbsolutePath());

      Class<? extends Bootstrapper> bsClass =
          ClassChangeUtil.compileAndLoad(sourceFile, packageName, className);

      return bsClass;

    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static class BootstrapClassloader extends ClassLoader {
    private BootstrapClassloader(ClassLoader classLoader) {
      super(classLoader);
    }

    public Class<?> defineClassX(String className, byte[] b, int off, int len) {
      return super.defineClass(className, b, off, len);
    }
  }
}
