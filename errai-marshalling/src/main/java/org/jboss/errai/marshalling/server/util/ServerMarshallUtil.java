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

package org.jboss.errai.marshalling.server.util;

import org.jboss.errai.marshalling.client.api.MarshallerFactory;
import org.jboss.errai.marshalling.rebind.MarshallerGeneratorFactory;
import org.jboss.errai.marshalling.rebind.MarshallerOuputTarget;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;

/**
 * @author Mike Brock
 */
public abstract class ServerMarshallUtil {

  public static Class<? extends MarshallerFactory> getGeneratedMarshallerFactoryForServer() {
    String packageName = MarshallerFactory.class.getPackage().getName();
    String className = "ServerMarshallingFactoryImpl";

    final String classStr = MarshallerGeneratorFactory.getFor(MarshallerOuputTarget.Java)
            .generate(packageName, className);


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

      String classBase = directory.getAbsolutePath() + "/" + className;

      File outFile = new File(classBase + ".class");

      if (directory.exists()) {
        for (File file : directory.listFiles()) {
          file.delete();
        }
        
        directory.delete();
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

      FileInputStream inputStream = new FileInputStream(outFile);
      byte[] classDefinition = new byte[inputStream.available()];

      inputStream.read(classDefinition);

      BootstrapClassloader clsLoader = new BootstrapClassloader(ClassLoader.getSystemClassLoader());

      Class<? extends MarshallerFactory> mainClass = (Class<? extends MarshallerFactory>) clsLoader
              .defineClassX(packageName + "." + className, classDefinition, 0, classDefinition.length);

      inputStream.close();

      for (int i = 1; i < Integer.MAX_VALUE; i++) {
        String innerClassBaseName = classBase + "$" + i;
        File innerClass = new File(innerClassBaseName + ".class");
        if (innerClass.exists()) {
          try {
            inputStream = new FileInputStream(innerClass);
            classDefinition = new byte[inputStream.available()];
            inputStream.read(classDefinition);

            clsLoader.defineClassX(packageName + "." + className + "$" + i, classDefinition, 0, classDefinition.length);
          }
          finally {
            inputStream.close();
          }
        }
        else {
          break;
        }
      }


      return mainClass;
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
