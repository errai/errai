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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import org.jboss.errai.codegen.meta.impl.gwt.GWTUtil;
import org.jboss.errai.codegen.util.ClassChangeUtil;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.rebind.ClassListReader;
import org.jboss.errai.common.rebind.EnvUtil;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;
import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.marshalling.server.ServerMappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallersGenerator extends Generator {

  private static final Logger logger = LoggerFactory.getLogger(Generator.class);

  public static final String SERVER_MARSHALLER_PACKAGE_NAME = "org.jboss.errai.marshalling.server.impl";
  public static final String SERVER_MARSHALLER_CLASS_NAME = "ServerMarshallingFactoryImpl";
  private static final String SERVER_MARSHALLER_OUTPUT_DIR_PROP = "errai.marshalling.server.classOutput";
  private static final String SERVER_MARSHALLER_OUTPUT_ENABLED_PROP = "errai.marshalling.server.classOutput.enabled";

  private static final String SERVER_MARSHALLER_OUTPUT_DIR =
          System.getProperty(SERVER_MARSHALLER_OUTPUT_DIR_PROP) != null ?
                  System.getProperty(SERVER_MARSHALLER_OUTPUT_DIR_PROP) :
                  null;

  private static final boolean SERVER_MARSHALLER_OUTPUT_ENABLED =
          Boolean.valueOf(System.getProperty(SERVER_MARSHALLER_OUTPUT_ENABLED_PROP, "true"));

  private static final String[] candidateOutputDirectories =
          {"target/classes/", "war/WEB-INF/classes/", "web/WEB-INF/classes/", "target/war/WEB-INF/classes/",
                  "WEB-INF/classes/", "src/main/webapp/WEB-INF/classes/"};

  private static final DiscoveryStrategy[] rootDiscoveryStrategies;

  private static Logger log = LoggerFactory.getLogger(MarshallersGenerator.class);

  static {
    // define the strategies which will be used to figure out where to desposit the server-side marshaller
    rootDiscoveryStrategies = new DiscoveryStrategy[]{
            new DiscoveryStrategy() {
              @Override
              public Set<String> getCandidate(GeneratorContext context, DiscoveryContext veto) {
                File cwd = new File("").getAbsoluteFile();
                Set<File> matching = ClassChangeUtil.findAllMatching("classlist.mf", cwd);
                Set<String> candidateDirectories = new HashSet<String>();

                veto.resultsAbsolute();

                if (!matching.isEmpty()) {

                  class Candidate {
                    int score;
                    File root;
                  }

                  Candidate bestCandidate = null;
                  String gwtModuleName = RebindUtils.getModuleName(context);

                  if (gwtModuleName != null) {

                    if (gwtModuleName.endsWith(".JUnit")) {
                      gwtModuleName = gwtModuleName.substring(0, gwtModuleName.length() - 6);
                    }
                    gwtModuleName = gwtModuleName.substring(0, gwtModuleName.lastIndexOf('.'));

                    for (File f : matching) {
                      Candidate candidate = new Candidate();
                      candidate.root = f.getParentFile();

                      Set<String> clazzes = ClassListReader.getClassSetFromFile(f);

                      for (String fqcn : clazzes) {

                        try {
                          JClassType type = context.getTypeOracle().findType(fqcn);

                          if (type != null && fqcn.startsWith(gwtModuleName)) {
                            candidate.score++;
                          }
                        }
                        catch (Throwable e) {
                        }
                      }

                      if (candidate.score > 0 && (bestCandidate == null || candidate.score > bestCandidate.score)) {
                        bestCandidate = candidate;
                      }
                    }

                    if (bestCandidate != null) {
                      candidateDirectories.add(bestCandidate.root.getAbsolutePath());
                    }
                  }
                }

                return candidateDirectories;
              }
            }
            ,
            new DiscoveryStrategy() {
              @Override
              public Set<String> getCandidate(GeneratorContext context, DiscoveryContext discoveryContext) {
                ServerMappingContext ctx = MappingContextSingleton.get();

                Map<String, String> matchNames = new HashMap<String, String>();

                for (Class<?> cls : ctx.getDefinitionsFactory().getExposedClasses()) {
                  matchNames.put(cls.getSimpleName(), cls.getName());
                }

                File cwd = new File("").getAbsoluteFile();

                Set<File> roots = ClassChangeUtil.findMatchingOutputDirectoryByModel(matchNames, cwd);

                if (!roots.isEmpty()) {
                  for (File file : roots) {
                    log.info(" ** signature matched root! " + file.getAbsolutePath());
                  }
                  discoveryContext.resultsAbsolute();
                }
                else {
                  log.warn(" ** NO ROOTS FOUND!");
                  discoveryContext.veto();
                }


                Set<String> rootsPaths = new HashSet<String>();
                for (File f : roots) {
                  rootsPaths.add(f.getAbsolutePath());
                }

                return rootsPaths;
              }
            },

            new DiscoveryStrategy() {
              @Override
              public Set<String> getCandidate(GeneratorContext context, DiscoveryContext veto) {
                // try the CWD
                return Collections.singleton(new File("").getAbsolutePath());
              }
            }
            ,
            new DiscoveryStrategy() {
              @Override
              public Set<String> getCandidate(GeneratorContext context, DiscoveryContext veto) {
                return Collections.singleton(RebindUtils.guessWorkingDirectoryForModule(context));
              }
            }
    };
  }

  /**
   * Simple name of class to be generated
   */
  private String className = null;

  /**
   * Package name of class to be generated
   */
  private String packageName = null;


  @Override
  public String generate(final TreeLogger logger, final GeneratorContext context, final String typeName)
          throws UnableToCompleteException {

    if (GWT.isProdMode()) {
      log.info("compiling in production mode.");
    }

    try {
      TypeOracle typeOracle = context.getTypeOracle();

      JClassType classType = typeOracle.getType(typeName);
      packageName = classType.getPackage().getName();
      className = classType.getSimpleSourceName() + "Impl";

      logger.log(TreeLogger.INFO, "Generating Marshallers Bootstrapper...");

      GWTUtil.populateMetaClassFactoryFromTypeOracle(context, logger);
      DefinitionsFactorySingleton.get().resetDefinitionsAndReload();

      // Generate class source code
      generateMarshallerBootstrapper(logger, context);
    }
    catch (Throwable e) {
      // record sendNowWith logger that Map generation threw an exception
      e.printStackTrace();
      logger.log(TreeLogger.ERROR, "Error generating marshallers", e);
    }

    // return the fully qualified name of the class generated
    return packageName + "." + className;
  }

  public void generateMarshallerBootstrapper(TreeLogger logger, GeneratorContext context) {
    PrintWriter printWriter = context.tryCreate(logger, packageName, className);
    if (printWriter == null) return;
    printWriter.write(_generate(context));
    context.commit(logger, printWriter);
  }

  private static final String sourceOutputTemp = RebindUtils.getTempDirectory() + "/errai.marshalling/gen/";


  private static volatile String _serverMarshallerCache;
  private static volatile String _clientMarshallerCache;
  private static final Object generatorLock = new Object();

  private String _generate(GeneratorContext context) {
    synchronized (generatorLock) {
      boolean junitOrDevMode = !EnvUtil.isProdMode();

      if (SERVER_MARSHALLER_OUTPUT_ENABLED && MarshallingGenUtil.isUseStaticMarshallers()) {

        String serverSideClass;
        if (!junitOrDevMode && _serverMarshallerCache != null) {
          serverSideClass = _serverMarshallerCache;
        }
        else {
          serverSideClass = MarshallerGeneratorFactory.getFor(MarshallerOuputTarget.Java)
                  .generate(SERVER_MARSHALLER_PACKAGE_NAME, SERVER_MARSHALLER_CLASS_NAME);
          _serverMarshallerCache = serverSideClass;
        }

        if (junitOrDevMode) {
          String tmpLocation = new File(sourceOutputTemp).getAbsolutePath();
          log.info("*** using temporary path: " + tmpLocation + " ***");

          String toLoad = generateServerMarshallers(tmpLocation, serverSideClass, tmpLocation);

          try {
            ClassChangeUtil.loadClassDefinition(toLoad, SERVER_MARSHALLER_PACKAGE_NAME, SERVER_MARSHALLER_CLASS_NAME);
          }
          catch (IOException e) {
            throw new RuntimeException("failed to load server marshallers", e);
          }

        }
        else if (SERVER_MARSHALLER_OUTPUT_DIR != null) {
          generateServerMarshallers(sourceOutputTemp, serverSideClass, SERVER_MARSHALLER_OUTPUT_DIR);
          logger.info("** deposited marshaller class in : " + new File(SERVER_MARSHALLER_OUTPUT_DIR).getAbsolutePath());
        }
        else {
          logger.debug("Searching candidate output directories for generated marshallers");
          File outputDirCdt;

          class DiscoveryContextImpl implements DiscoveryContext {
            boolean vetoed = false;
            boolean absolute = false;

            @Override
            public void veto() {
              this.vetoed = true;
            }

            @Override
            public void resultsAbsolute() {
              this.absolute = true;
            }
          }

          int deposits = 0;


          Strategies:
          for (DiscoveryStrategy strategy : rootDiscoveryStrategies) {
            DiscoveryContextImpl discoveryContext = new DiscoveryContextImpl();
            for (String rootPath : strategy.getCandidate(context, discoveryContext)) {
              for (String candidate : discoveryContext.absolute ? new String[]{"/"} : candidateOutputDirectories) {
                logger.info("considering '" + rootPath + candidate + "' as module output path ...");

                if (discoveryContext.vetoed) {
                  continue Strategies;
                }

                outputDirCdt = new File(rootPath + "/" + candidate).getAbsoluteFile();
                if (outputDirCdt.exists()) {
                  logger.info("   found '" + outputDirCdt + "' output directory");

                  generateServerMarshallers(sourceOutputTemp, serverSideClass, outputDirCdt.getAbsolutePath());
                  logger.info("** deposited marshaller class in : " + outputDirCdt.getAbsolutePath());
                  deposits++;
                }
                else {
                  logger.debug("   " + outputDirCdt + " does not exist");
                }
              }
            }
            if (deposits > 0) {
              break;
            }
          }

          if (deposits == 0) {
            logger.warn(" *** the server marshaller was not deposited into your build output!\n" +
                    "   A target output could not be resolved through configuration or auto-detection!");
          }
        }
      }
      else {
        logger.info("not emitting server marshaller class");
      }

      if (!junitOrDevMode && _clientMarshallerCache != null) {
        return _clientMarshallerCache;
      }

      return _clientMarshallerCache
              = MarshallerGeneratorFactory.getFor(MarshallerOuputTarget.GWT).generate(packageName, className);
    }
  }

  interface DiscoveryContext {
    public void veto();

    public void resultsAbsolute();
  }

  interface DiscoveryStrategy {
    public Set<String> getCandidate(GeneratorContext context, DiscoveryContext veto);
  }


  private String generateServerMarshallers(String sourceDir, String serverSideClass, String outputPath) {
    File outputDir = new File(sourceDir + File.separator +
            RebindUtils.packageNameToDirName(SERVER_MARSHALLER_PACKAGE_NAME) + File.separator);

    File classOutputPath = new File(outputPath);

    outputDir.mkdirs();

    File sourceFile = new File(outputDir.getAbsolutePath() + File.separator + SERVER_MARSHALLER_CLASS_NAME + ".java");

    RebindUtils.writeStringToFile(sourceFile, serverSideClass);

    ClassChangeUtil.compileClass(outputDir.getAbsolutePath(),
            SERVER_MARSHALLER_PACKAGE_NAME,
            SERVER_MARSHALLER_CLASS_NAME,
            classOutputPath.getAbsolutePath());

    return new File(outputDir.getAbsolutePath() + File.separator + SERVER_MARSHALLER_CLASS_NAME + ".class").getAbsolutePath();
  }
}
