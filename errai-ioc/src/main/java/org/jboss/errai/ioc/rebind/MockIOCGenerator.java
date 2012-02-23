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

package org.jboss.errai.ioc.rebind;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.ioc.client.api.Bootstrapper;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCBootstrapGenerator;
import org.jboss.errai.marshalling.server.util.ServerMarshallUtil;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MockIOCGenerator {
  private List<String> packages;

  public MockIOCGenerator(List<String> packages) {
    this.packages = Assert.notNull(packages);
  }

  public Class<? extends Bootstrapper> generate() {
    String packageName = Bootstrapper.class.getPackage().getName();
    String className = "MockBootstrapperImpl";

    IOCBootstrapGenerator bootstrapGenerator = new IOCBootstrapGenerator();
    bootstrapGenerator.setUseReflectionStubs(true);
    bootstrapGenerator.setPackages(packages);

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

      outputStream.write(classStr.getBytes());
      outputStream.flush();
      outputStream.close();

      System.out.println("wrote file: " + sourceFile.getAbsolutePath());

      ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();

      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

      compiler.run(null, null, errorOutputStream, sourceFile.getAbsolutePath());

      for (byte b : errorOutputStream.toByteArray()) {
        System.out.print((char) b);
      }

      Class<? extends Bootstrapper> bsClass
              = ServerMarshallUtil.loadClassDefinition(outFile.getAbsolutePath(), packageName, className);

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
