package org.jboss.errai.ioc.rebind;


import org.jboss.errai.ioc.client.api.Bootstrapper;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCBootstrapGenerator;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MockIOCGenerator {
  private String packageFilter;

  public Class<? extends Bootstrapper> generate() {
    IOCBootstrapGenerator bootstrapGenerator = new IOCBootstrapGenerator();
    bootstrapGenerator.setUseReflectionStubs(true);
    bootstrapGenerator.setPackageFilter(packageFilter);

    String packageName = Bootstrapper.class.getPackage().getName();
    String className = "MockBootstrapperImpl";

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

      FileInputStream inputStream = new FileInputStream(outFile);

      byte[] classDefinition = new byte[inputStream.available()];

      inputStream.read(classDefinition);

      return (Class<? extends Bootstrapper>) new BootstrapClassloader(ClassLoader.getSystemClassLoader())
              .defineClassX(packageName + "." + className, classDefinition, 0, classDefinition.length);
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
