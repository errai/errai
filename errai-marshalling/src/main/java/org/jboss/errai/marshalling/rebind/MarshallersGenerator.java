package org.jboss.errai.marshalling.rebind;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

import java.io.*;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallersGenerator extends Generator {
  /**
   * Simple name of class to be generated
   */
  private String className = null;

  /**
   * Package name of class to be generated
   */
  private String packageName = null;
  private TypeOracle typeOracle;
  private String modulePackage;

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
    try {
      typeOracle = context.getTypeOracle();

      JClassType classType = typeOracle.getType(typeName);
      packageName = classType.getPackage().getName();
      className = classType.getSimpleSourceName() + "Impl";

      logger.log(TreeLogger.INFO, "Generating Marshallers Bootstrapper...");

      // Generate class source code
      generateMarshallerBootstrapper(logger, context);
    }
    catch (Throwable e) {
      // record sendNowWith logger that Map generation threw an exception
      e.printStackTrace();
      logger.log(TreeLogger.ERROR, "Error generating marshallers", e);
    }

    System.out.println("**FIN**");

    // return the fully qualified name of the class generated
    return packageName + "." + className;
  }

  public void generateMarshallerBootstrapper(TreeLogger logger, GeneratorContext context) {
    PrintWriter printWriter = context.tryCreate(logger, packageName, className);

    if (printWriter == null) return;

    boolean debugCache = Boolean.getBoolean("errai.marshalling.debugCache");
    String cacheDir = System.getProperty("errai.marshalling.debugCacheDir");

    if (cacheDir == null) cacheDir = new File("").getAbsolutePath();
    File cacheFile = new File(cacheDir + "/" + className + ".java");
    String gen;
    
    if (debugCache) {
      if (!cacheFile.exists()) {
        gen = _generate();

        try {
          FileOutputStream outputStream = new FileOutputStream(cacheFile, false);
          outputStream.write(gen.getBytes());
          outputStream.close();

          System.out.println("*SAVED CACHE*");
        }
        catch (IOException e) {
          throw new RuntimeException("could not write file for debug cache", e);
        }
      }
      else {
        StringBuilder buf = new StringBuilder();
        try {
          FileInputStream inputStream = new FileInputStream(cacheFile);
          byte[] b = new byte[1024];
          int read;
          while ((read = inputStream.read(b)) != -1) {
             for (int i = 0; i < read; i++) {
               buf.append((char) b[i]);
             }
          }
          inputStream.close();
          System.out.println("*READ CACHE*");
          
        }
        catch (FileNotFoundException e) {
          throw new RuntimeException("could not read file for debug cache", e);
        }
        catch (IOException e) {
          throw new RuntimeException("could not read file for debug cache", e);
        }

        gen = buf.toString();
      }
    }
    else {
      if (cacheFile.exists()) {
        cacheFile.delete();
      }
      
      gen = _generate();
    }

    printWriter.write(gen);

    //  printWriter.write();


    context.commit(logger, printWriter);
  }

  private String _generate() {
    return new MarshallerGeneratorFactory().generate(packageName, className);
  }
}
