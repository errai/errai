/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.marshalling.rebind.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.util.ClassChangeUtil;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.rebind.ClassListReader;
import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.marshalling.server.ServerMappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.typeinfo.JClassType;

/**
 * Discovers directories where generated class files can be written.
 */
public class OutputDirectoryUtil {

  private static Logger log = LoggerFactory.getLogger(OutputDirectoryUtil.class);

  private static final String[] candidateOutputDirectories =
  {
      "target/classes/",
      "war/WEB-INF/classes/",
      "web/WEB-INF/classes/",
      "target/war/WEB-INF/classes/",
      "WEB-INF/classes/",
      "src/main/webapp/WEB-INF/classes/"
  };

  private static final DiscoveryStrategy[] rootDiscoveryStrategies = new DiscoveryStrategy[]{
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
        },
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

  public static void forEachDiscoveredOutputDir(final GeneratorContext context, final Consumer<File> consumer) {
    log.debug("searching candidate output directories for generated marshallers");
    File outputDirCdt;

    int deposits = 0;

    Strategies:
    for (final DiscoveryStrategy strategy : rootDiscoveryStrategies) {
      final DiscoveryContext discoveryContext = DiscoveryContext.create();
      for (final String rootPath : strategy.getCandidate(context, discoveryContext)) {
        for (final String candidate : discoveryContext.isAbsolute() ? new String[]{"/"} : candidateOutputDirectories) {
          log.info("considering '" + rootPath + candidate + "' as module output path ...");

          if (discoveryContext.isVetoed()) {
            continue Strategies;
          }

          outputDirCdt = new File(rootPath + "/" + candidate).getAbsoluteFile();
          if (outputDirCdt.exists()) {
            log.info("   found '" + outputDirCdt + "' output directory");
            consumer.accept(outputDirCdt);
            deposits++;
          }
          else {
            log.debug("   " + outputDirCdt + " does not exist");
          }
        }
      }
      if (deposits > 0) {
        break;
      }
    }

    if (deposits == 0) {
      log.warn(" *** the server marshaller was not deposited into your build output!\n" +
          "   A target output could not be resolved through configuration or auto-detection!");
    }
  }

  public static void generateClassFileInDiscoveredDirs(final GeneratorContext context, final String packageName, final String simpleClassName,
          final String sourceOutputTemp, final String source) {
    forEachDiscoveredOutputDir(context, outputDirCdt -> {
      ClassChangeUtil.generateClassFile(packageName, simpleClassName, sourceOutputTemp,
              source, outputDirCdt.getAbsolutePath());
      log.info("** deposited marshaller class in : " + outputDirCdt.getAbsolutePath());
    });
  }

  public static void generateClassFileInTmpDir(final String packageName, final String simpleClassName, final String source, final String tmpDirPath) {
    final String classFilePath = ClassChangeUtil.generateClassFile(packageName, simpleClassName, tmpDirPath, source, tmpDirPath);
    try {
      ClassChangeUtil.loadClassDefinition(classFilePath, packageName, simpleClassName);
    } catch (IOException e) {
      throw new RuntimeException("Could not load " + packageName + "." + simpleClassName, e);
    }
  }

}
