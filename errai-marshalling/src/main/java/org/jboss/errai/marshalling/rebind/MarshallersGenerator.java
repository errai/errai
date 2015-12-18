/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.util.ClassChangeUtil;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.rebind.ClassListReader;
import org.jboss.errai.config.rebind.AbstractAsyncGenerator;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.config.rebind.GenerateAsync;
import org.jboss.errai.marshalling.client.api.MarshallerFactory;
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

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@GenerateAsync(MarshallerFactory.class)
public class MarshallersGenerator extends AbstractAsyncGenerator {
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
      {
          "target/classes/",
          "war/WEB-INF/classes/",
          "web/WEB-INF/classes/",
          "target/war/WEB-INF/classes/",
          "WEB-INF/classes/",
          "src/main/webapp/WEB-INF/classes/"
      };

  private static final DiscoveryStrategy[] rootDiscoveryStrategies;

  private static final Logger log = LoggerFactory.getLogger(MarshallersGenerator.class);

  static {
    // define the strategies which will be used to figure out where to deposit the server-side marshaller
    rootDiscoveryStrategies = new DiscoveryStrategy[]{
        new DiscoveryStrategy() {
          @Override
          public Set<String> getCandidate(final GeneratorContext context, final DiscoveryContext veto) {
            final File cwd = new File("").getAbsoluteFile();
            final Set<File> matching = ClassChangeUtil.findAllMatching("classlist.mf", cwd);
            final Set<String> candidateDirectories = new HashSet<String>();

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
                final int endIndex = gwtModuleName.lastIndexOf('.');
                if (endIndex != -1) {
                  gwtModuleName = gwtModuleName.substring(0, endIndex);
                }
                else {
                  gwtModuleName = "";
                }

                for (final File f : matching) {
                  final Candidate candidate = new Candidate();
                  candidate.root = f.getParentFile();

                  final Set<String> clazzes = ClassListReader.getClassSetFromFile(f);

                  for (final String fqcn : clazzes) {

                    try {
                      final JClassType type = context.getTypeOracle().findType(fqcn);

                      if (type != null && fqcn.startsWith(gwtModuleName)) {
                        candidate.score++;
                      }
                    }
                    catch (Throwable ignored) {
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
          public Set<String> getCandidate(final GeneratorContext context,
                                          final DiscoveryContext discoveryContext) {

            final ServerMappingContext ctx = MappingContextSingleton.get();

            final Map<String, String> matchNames = new HashMap<String, String>();

            for (final MetaClass cls : ctx.getDefinitionsFactory().getExposedClasses()) {
              matchNames.put(cls.getName(), cls.getName());
            }

            final File cwd = new File("").getAbsoluteFile();

            final Set<File> roots = ClassChangeUtil.findMatchingOutputDirectoryByModel(matchNames, cwd);

            if (!roots.isEmpty()) {
              for (final File file : roots) {
                log.info(" ** signature matched root! " + file.getAbsolutePath());
              }
              discoveryContext.resultsAbsolute();
            }
            else {
              log.warn(" ** NO ROOTS FOUND!");
              discoveryContext.veto();
            }


            final Set<String> rootsPaths = new HashSet<String>();
            for (final File f : roots) {
              rootsPaths.add(f.getAbsolutePath());
            }

            return rootsPaths;
          }
        },

        new DiscoveryStrategy() {
          @Override
          public Set<String> getCandidate(final GeneratorContext context,
                                          final DiscoveryContext veto) {
            // try the CWD
            return Collections.singleton(new File("").getAbsolutePath());
          }
        }
        ,
        new DiscoveryStrategy() {
          @Override
          public Set<String> getCandidate(final GeneratorContext context,
                                          final DiscoveryContext veto) {

            return Collections.singleton(RebindUtils.guessWorkingDirectoryForModule(context));
          }
        }
    };
  }

  /**
   * Simple name of class to be generated
   */
  private final String className = MarshallerFactory.class.getSimpleName() + "Impl";

  /**
   * Package name of class to be generated
   */
  private final String packageName = MarshallerFactory.class.getPackage().getName();

  @Override
  public String generate(final TreeLogger logger, final GeneratorContext context, final String typeName)
      throws UnableToCompleteException {
    logger.log(TreeLogger.INFO, "Generating Marshallers Bootstrapper...");
    return startAsyncGeneratorsAndWaitFor(MarshallerFactory.class, context, logger, packageName, className);
  }

  private static final String sourceOutputTemp = RebindUtils.getTempDirectory() + "/errai.marshalling/gen/";

  private static volatile String _serverMarshallerCache;
  private static volatile String _clientMarshallerCache;
  private static final Object generatorLock = new Object();

  @Override
  protected String generate(final TreeLogger treeLogger, final GeneratorContext context) {
    synchronized (generatorLock) {
      final boolean junitOrDevMode = !EnvUtil.isProdMode();

      if (SERVER_MARSHALLER_OUTPUT_ENABLED && MarshallingGenUtil.isUseStaticMarshallers()) {

        final String serverSideClass;
        if (!junitOrDevMode && _serverMarshallerCache != null) {
          serverSideClass = _serverMarshallerCache;
        }
        else {
          serverSideClass = MarshallerGeneratorFactory.getFor(context, MarshallerOutputTarget.Java)
              .generate(SERVER_MARSHALLER_PACKAGE_NAME, SERVER_MARSHALLER_CLASS_NAME);
          _serverMarshallerCache = serverSideClass;
        }

        if (junitOrDevMode) {
          if (MarshallingGenUtil.isUseStaticMarshallers()) {
            final String tmpLocation = new File(sourceOutputTemp).getAbsolutePath();
            log.info("*** using temporary path: " + tmpLocation + " ***");

            final String toLoad = generateServerMarshallers(tmpLocation, serverSideClass, tmpLocation);

            try {
              ClassChangeUtil.loadClassDefinition(toLoad, SERVER_MARSHALLER_PACKAGE_NAME, SERVER_MARSHALLER_CLASS_NAME);
            }
            catch (IOException e) {
              throw new RuntimeException("failed to load server marshallers", e);
            }
          }
        }
        else if (SERVER_MARSHALLER_OUTPUT_DIR != null) {
          generateServerMarshallers(sourceOutputTemp, serverSideClass, SERVER_MARSHALLER_OUTPUT_DIR);
          logger.info("** deposited marshaller class in : " + new File(SERVER_MARSHALLER_OUTPUT_DIR).getAbsolutePath());
        }
        else {
          logger.debug("searching candidate output directories for generated marshallers");
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
          for (final DiscoveryStrategy strategy : rootDiscoveryStrategies) {
            final DiscoveryContextImpl discoveryContext = new DiscoveryContextImpl();
            for (final String rootPath : strategy.getCandidate(context, discoveryContext)) {
              for (final String candidate : discoveryContext.absolute ? new String[]{"/"} : candidateOutputDirectories) {
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
          = MarshallerGeneratorFactory.getFor(context, MarshallerOutputTarget.GWT)
          .generate(packageName, className, new MarshallerGenerationCallback() {

            @Override
            public void callback(final MetaClass marshalledType) {
              addCacheRelevantClass(marshalledType);
            }
          });
    }
  }

  @Override
  protected boolean isRelevantClass(final MetaClass clazz) {
    return EnvUtil.isPortableType(clazz);
  }

  interface DiscoveryContext {
    public void veto();

    public void resultsAbsolute();
  }

  interface DiscoveryStrategy {
    public Set<String> getCandidate(GeneratorContext context, DiscoveryContext veto);
  }

  private String generateServerMarshallers(final String sourceDir,
                                           final String serverSideClass,
                                           final String outputPath) {

    final File outputDir = new File(sourceDir + File.separator +
        RebindUtils.packageNameToDirName(SERVER_MARSHALLER_PACKAGE_NAME) + File.separator);

    final File classOutputPath = new File(outputPath);

    //noinspection ResultOfMethodCallIgnored
    outputDir.mkdirs();

    final File sourceFile
        = new File(outputDir.getAbsolutePath() + File.separator + SERVER_MARSHALLER_CLASS_NAME + ".java");

    RebindUtils.writeStringToFile(sourceFile, serverSideClass);

    ClassChangeUtil.compileClass(outputDir.getAbsolutePath(),
        SERVER_MARSHALLER_PACKAGE_NAME,
        SERVER_MARSHALLER_CLASS_NAME,
        classOutputPath.getAbsolutePath());

    return new File(outputDir.getAbsolutePath() + File.separator + SERVER_MARSHALLER_CLASS_NAME + ".class")
        .getAbsolutePath();
  }
}
