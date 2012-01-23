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

package org.jboss.errai.marshalling.rebind;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.rebind.EnvironmentUtil;
import org.jboss.errai.marshalling.server.util.ServerMarshallUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallersGenerator extends Generator {
  public static final String SERVER_MARSHALLER_PACKAGE_NAME = "org.jboss.errai.marshalling.server.impl";
  public static final String SERVER_MARSHALLER_CLASS_NAME = "ServerMarshallingFactoryImpl";
  private static final String SERVER_MARSHALLER_OUTPUT_DIR_PROP = "errai.marshalling.server.classOutput";
  private static final String SERVER_MARSHALLER_OUTPUT_ENABLED_PROP = "errai.marshalling.server.classOutput.enabled";

  private static final String SERVER_MARSHALLER_OUTPUT_DIR =
          System.getProperty(SERVER_MARSHALLER_OUTPUT_DIR_PROP) != null ?
                  System.getProperty(SERVER_MARSHALLER_OUTPUT_DIR_PROP) :
                  null;

  private static final boolean SERVER_MARSHALLER_OUTPUT_ENABLED =
          System.getProperty(SERVER_MARSHALLER_OUTPUT_ENABLED_PROP) == null
                  || Boolean.getBoolean(SERVER_MARSHALLER_OUTPUT_ENABLED_PROP);

  private static final String[] candidateOutputDirectories =
          {"target/classes/", "war/WEB-INF/classes/", "web/WEB-INF/classes/", "target/war/WEB-INF/classes/", "WEB-INF/classes/"};

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
  public String generate(final TreeLogger logger, final GeneratorContext context, final String typeName)
          throws UnableToCompleteException {
    
    final Thread marshallGenThread = new Thread() {
      @Override
      public void run() {
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
      }
    };

    marshallGenThread.start();

    try {
      marshallGenThread.join();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    // return the fully qualified name of the class generated
    return packageName + "." + className;
  }

  public void generateMarshallerBootstrapper(TreeLogger logger, GeneratorContext context) {
    PrintWriter printWriter = context.tryCreate(logger, packageName, className);
    if (printWriter == null) return;
    printWriter.write(_generate());
    context.commit(logger, printWriter);
  }

  private String _generate() {
    boolean junit = EnvironmentUtil.isGWTJUnitTest();

    if (junit) {
      System.out.println("******** running inside JUnit! ********");
    }

    if (SERVER_MARSHALLER_OUTPUT_ENABLED) {
      String serverSideClass = MarshallerGeneratorFactory.getFor(MarshallerOuputTarget.Java)
              .generate(SERVER_MARSHALLER_PACKAGE_NAME, SERVER_MARSHALLER_CLASS_NAME);


      if (junit) {
        Random rand = new Random(System.nanoTime());
        String tmpLocation = new File(System.getProperty("java.io.tmpdir") + "/" + rand.nextInt(Integer.MAX_VALUE)
                + "/errai.marshalling/out/").getAbsolutePath();
        System.out.println("*** using temporary path for JUnit Shell: " + tmpLocation + " ***");

        String toLoad = generateServerMarshallers(tmpLocation, serverSideClass);

        try {
          ServerMarshallUtil.loadClassDefinition(toLoad, SERVER_MARSHALLER_PACKAGE_NAME, SERVER_MARSHALLER_CLASS_NAME);
        }
        catch (IOException e) {
          throw new RuntimeException("failed to load server marshallers", e);
        }

      }
      else if (SERVER_MARSHALLER_OUTPUT_DIR != null) {
        generateServerMarshallers(SERVER_MARSHALLER_OUTPUT_DIR, serverSideClass);
      }
      else {
        File outputDirCdt;
        for (String candidate : candidateOutputDirectories) {
          outputDirCdt = new File(candidate);
          if (outputDirCdt.exists()) {
            generateServerMarshallers(outputDirCdt.getAbsolutePath(), serverSideClass);
            System.out.println("** deposited marshaller class in : " + outputDirCdt.getAbsolutePath());
          }
        }
      }
    }


    return MarshallerGeneratorFactory.getFor(MarshallerOuputTarget.GWT).generate(packageName, className);
  }

  private String generateServerMarshallers(String dir, String serverSideClass) {
    File outputDir = new File(dir + File.separator +
            RebindUtils.packageNameToDirName(SERVER_MARSHALLER_PACKAGE_NAME) + File.separator);
    outputDir.mkdirs();

    File sourceFile = new File(outputDir.getAbsolutePath() + File.separator + SERVER_MARSHALLER_CLASS_NAME + ".java");

    RebindUtils.writeStringToFile(sourceFile,
            serverSideClass);

    ServerMarshallUtil.compileClass(outputDir.getAbsolutePath(), SERVER_MARSHALLER_PACKAGE_NAME, SERVER_MARSHALLER_CLASS_NAME);

    return new File(outputDir.getAbsolutePath() + File.separator + SERVER_MARSHALLER_CLASS_NAME + ".class").getAbsolutePath();
  }
}
