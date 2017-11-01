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

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.util.ClassChangeUtil;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.propertiesfile.ErraiAppPropertiesConfiguration;
import org.jboss.errai.config.ErraiConfiguration;
import org.jboss.errai.config.marshalling.MarshallingConfiguration;
import org.jboss.errai.config.rebind.AbstractAsyncGenerator;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.config.rebind.GenerateAsync;
import org.jboss.errai.marshalling.client.api.MarshallerFactory;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;
import org.jboss.errai.marshalling.rebind.util.OutputDirectoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@GenerateAsync(MarshallerFactory.class)
public class MarshallersGenerator extends AbstractAsyncGenerator {

  private static final Logger logger = LoggerFactory.getLogger(Generator.class);
  private static final Logger log = LoggerFactory.getLogger(MarshallersGenerator.class);

  private static final String SERVER_MARSHALLER_OUTPUT_DIR_PROP = "errai.marshalling.server.classOutput";
  private static final String SERVER_MARSHALLER_OUTPUT_ENABLED_PROP = "errai.marshalling.server.classOutput.enabled";
  private static final String SERVER_MARSHALLER_OUTPUT_DIR = System.getProperty(SERVER_MARSHALLER_OUTPUT_DIR_PROP);

  private static final boolean SERVER_MARSHALLER_OUTPUT_ENABLED =
      Boolean.valueOf(System.getProperty(SERVER_MARSHALLER_OUTPUT_ENABLED_PROP, "true"));

  public static final String SERVER_PACKAGE_NAME = "org.jboss.errai";
  public static final String SERVER_CLASS_NAME = "ServerMarshallingFactoryImpl";
  public static final String CLIENT_PACKAGE_NAME = "org.jboss.errai.marshalling.client.api";
  public static final String CLIENT_CLASS_NAME = "MarshallerFactoryImpl";

  @Override
  public String generate(final TreeLogger logger, final GeneratorContext context, final String typeName)
      throws UnableToCompleteException {
    logger.log(TreeLogger.INFO, "Generating Marshallers Bootstrapper...");
    return startAsyncGeneratorsAndWaitFor(MarshallerFactory.class, context, logger, CLIENT_PACKAGE_NAME,
            CLIENT_CLASS_NAME);
  }

  private static final String sourceOutputTemp = RebindUtils.getTempDirectory() + "/errai.marshalling/gen/";

  private static volatile String _serverMarshallerCache;
  private static volatile String _clientMarshallerCache;
  private static final Object generatorLock = new Object();

  @Override
  protected String generate(final TreeLogger treeLogger, final GeneratorContext context) {
    synchronized (generatorLock) {
      final boolean junitOrDevMode = !EnvUtil.isProdMode();
      final ErraiConfiguration erraiConfiguration = new ErraiAppPropertiesConfiguration();

      if (SERVER_MARSHALLER_OUTPUT_ENABLED && MarshallingGenUtil.isUseStaticMarshallers(erraiConfiguration)) {

        final String serverSource;
        if (!junitOrDevMode && _serverMarshallerCache != null) {
          serverSource = _serverMarshallerCache;
        }
        else {
          serverSource = MarshallerGeneratorFactory.getFor(context, MarshallerOutputTarget.Java, erraiConfiguration)
              .generate(SERVER_PACKAGE_NAME, SERVER_CLASS_NAME);
          _serverMarshallerCache = serverSource;
        }

        if (junitOrDevMode) {
          if (MarshallingGenUtil.isUseStaticMarshallers(erraiConfiguration)) {
            final String tmpLocation = new File(sourceOutputTemp).getAbsolutePath();
            log.info("*** using temporary path: " + tmpLocation + " ***");

            try {
              OutputDirectoryUtil.generateClassFileInTmpDir(SERVER_PACKAGE_NAME, SERVER_CLASS_NAME, serverSource, tmpLocation);
            }
            catch (final Throwable t) {
              throw new RuntimeException("failed to load server marshallers", t);
            }
          }
        }
        else if (SERVER_MARSHALLER_OUTPUT_DIR != null || OutputDirectoryUtil.OUTPUT_DIR.isPresent()) {
          final String outputDir = (SERVER_MARSHALLER_OUTPUT_DIR != null ? SERVER_MARSHALLER_OUTPUT_DIR : OutputDirectoryUtil.OUTPUT_DIR.get());
          ClassChangeUtil.generateClassFile(SERVER_PACKAGE_NAME, SERVER_CLASS_NAME, sourceOutputTemp, serverSource, outputDir);
          logger.info("** deposited marshaller class in : " + new File(outputDir).getAbsolutePath());
        }
        else {
          writeServerSideMarshallerToDiscoveredOutputDirs(context, serverSource);
        }
      }
      else {
        logger.info("not emitting server marshaller class");
      }

      if (!junitOrDevMode && _clientMarshallerCache != null) {
        return _clientMarshallerCache;
      }

      return _clientMarshallerCache = MarshallerGeneratorFactory.getFor(context, MarshallerOutputTarget.GWT,
              erraiConfiguration).generate(CLIENT_PACKAGE_NAME, CLIENT_CLASS_NAME, this::addCacheRelevantClass);
    }
  }

  private static void writeServerSideMarshallerToDiscoveredOutputDirs(final GeneratorContext context, final String source) {
    OutputDirectoryUtil.generateClassFileInDiscoveredDirs(context, SERVER_PACKAGE_NAME, SERVER_CLASS_NAME, sourceOutputTemp, source);
  }

  @Override
  protected boolean isRelevantClass(final MetaClass clazz) {
    return MarshallingConfiguration.isPortableType(clazz.unsafeAsClass());
  }

  @Override
  public boolean alreadyGeneratedSourcesViaAptGenerators(final GeneratorContext context) {
    return RebindUtils.isErraiUseAptGeneratorsPropertyEnabled(context);
  }

}
