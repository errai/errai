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
import com.google.gwt.dev.javac.testing.GeneratorContextBuilder;
import org.jboss.errai.codegen.util.ClassChangeUtil;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.ioc.client.Bootstrapper;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCBootstrapGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Set;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MockIOCGenerator {

  private final Set<String> packages;

  public MockIOCGenerator(final Set<String> packages) {
    this.packages = Assert.notNull(packages);
  }

  public Class<? extends Bootstrapper> generate() {
    ClassScanner.setReflectionsScanning(true);

    final String packageName = Bootstrapper.class.getPackage().getName();
    final String className = "MockBootstrapperImpl";

    final IOCBootstrapGenerator bootstrapGenerator = new IOCBootstrapGenerator(GeneratorContextBuilder.newCoreBasedBuilder().buildGeneratorContext(),
            new TreeLogger() {
                  @Override
                  public TreeLogger branch(final Type type, final String msg, final Throwable caught, final HelpInfo helpInfo) {
                    return null;
                  }

                  @Override
                  public boolean isLoggable(final Type type) {
                    return false;
                  }

                  @Override
                  public void log(final Type type, final String msg, final Throwable caught, final HelpInfo helpInfo) {
                    System.out.println(type.getLabel() + ": " + msg);
                    if (caught != null) {
                      caught.printStackTrace();
                    }
                  }
                }, packages, true);


    final String classStr = bootstrapGenerator.generate(packageName, className);

    final File fileCacheDir = RebindUtils.getErraiCacheDir();
    final File cacheFile = new File(fileCacheDir.getAbsolutePath() + "/" + className + ".java");

    RebindUtils.writeStringToFile(cacheFile, classStr);

    try {
      final File directory =
              new File(RebindUtils.getTempDirectory() + "/ioc/classes/" + packageName.replaceAll("\\.", "/"));

      final File sourceFile = new File(directory.getAbsolutePath() + "/" + className + ".java");
      final File outFile = new File(directory.getAbsolutePath() + "/" + className + ".class");

      if (sourceFile.exists()) {
        sourceFile.delete();
        outFile.delete();
      }

      directory.mkdirs();

      final FileOutputStream outputStream = new FileOutputStream(sourceFile);

      outputStream.write(classStr.getBytes("UTF-8"));
      outputStream.flush();
      outputStream.close();

      System.out.println("wrote file: " + sourceFile.getAbsolutePath());

      final Class<? extends Bootstrapper> bsClass =
          ClassChangeUtil.compileAndLoad(sourceFile, packageName, className);

      return bsClass;

    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    finally {
      ClassScanner.setReflectionsScanning(false);
    }
  }

  private static class BootstrapClassloader extends ClassLoader {
    private BootstrapClassloader(final ClassLoader classLoader) {
      super(classLoader);
    }

    public Class<?> defineClassX(final String className, final byte[] b, final int off, final int len) {
      return super.defineClass(className, b, off, len);
    }
  }
}
