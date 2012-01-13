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


import org.jboss.errai.ioc.client.api.Bootstrapper;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCBootstrapGenerator;
import org.jboss.errai.marshalling.server.util.ServerMarshallUtil;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MockIOCGenerator {
  private String packageFilter;

  public Class<? extends Bootstrapper> generate() {
    String packageName = Bootstrapper.class.getPackage().getName();
    String className = "MockBootstrapperImpl";

    IOCBootstrapGenerator bootstrapGenerator = new IOCBootstrapGenerator();
    bootstrapGenerator.setUseReflectionStubs(true);
    bootstrapGenerator.setPackageFilter(packageFilter);


    final String classStr = bootstrapGenerator.generate(packageName, className);

    InputStream inStream = new InputStream() {
      int cursor = 0;

      @Override
      public int read() throws IOException {
        if (cursor == classStr.length()) {
          return -1;
        }

        return classStr.charAt(cursor++);
      }
    };

    System.out.println(classStr);

    try {
      File directory =
              new File(System.getProperty("java.io.tmpdir") + "/out/classes/" + packageName.replaceAll("\\.", "/"));

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

//      FileInputStream inputStream = new FileInputStream(outFile);
//
//      byte[] classDefinition = new byte[inputStream.available()];
//
//      inputStream.read(classDefinition);
//
//      return (Class<? extends Bootstrapper>) new BootstrapClassloader(ClassLoader.getSystemClassLoader())
//              .defineClassX(packageName + "." + className, classDefinition, 0, classDefinition.length);
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

  public void setPackageFilter(String packageFilter) {
    this.packageFilter = packageFilter;
  }
}
