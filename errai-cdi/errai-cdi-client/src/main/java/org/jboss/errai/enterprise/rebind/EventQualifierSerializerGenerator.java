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

package org.jboss.errai.enterprise.rebind;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.util.ClassChangeUtil;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.rebind.AbstractAsyncGenerator;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.config.rebind.GenerateAsync;
import org.jboss.errai.enterprise.client.cdi.EventQualifierSerializer;
import org.jboss.errai.ioc.util.TranslatableAnnotationUtils;
import org.jboss.errai.marshalling.rebind.util.OutputDirectoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Qualifier;
import java.io.File;

import static org.jboss.errai.enterprise.client.cdi.EventQualifierSerializer.SERIALIZER_CLASS_NAME;
import static org.jboss.errai.enterprise.client.cdi.EventQualifierSerializer.SERIALIZER_PACKAGE_NAME;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@GenerateAsync(EventQualifierSerializer.class)
public class EventQualifierSerializerGenerator extends AbstractAsyncGenerator {

  private static final Logger logger = LoggerFactory.getLogger(EventQualifierSerializerGenerator.class);

  private static final String OUTPUT_TMP = RebindUtils.getTempDirectory() + File.separator + "errai.cdi" + File.separator + "gen";
  private static final String SOURCE_OUTPUT_TMP = OUTPUT_TMP + File.separator + "event-qualifier-serializer";

  @Override
  public String generate(final TreeLogger logger, final GeneratorContext context, final String typeName)
          throws UnableToCompleteException {
    return startAsyncGeneratorsAndWaitFor(EventQualifierSerializer.class, context, logger,
            SERIALIZER_PACKAGE_NAME, SERIALIZER_CLASS_NAME);
  }

  @Override
  protected String generate(final TreeLogger treeLogger, final GeneratorContext context) {
    logger.info("Generating {}.{}...", SERIALIZER_PACKAGE_NAME, SERIALIZER_CLASS_NAME);
    final String source = NonGwtEventQualifierSerializerGenerator.generateSource(TranslatableAnnotationUtils.getTranslatableQualifiers(context.getTypeOracle()));

    logger.info("Generating class file for server.");
    if (EnvUtil.isProdMode()) {
      if (OutputDirectoryUtil.OUTPUT_DIR.isPresent()) {
        logger.info("Output directory set to {}. Attempting to write class file to this directory.", OutputDirectoryUtil.OUTPUT_DIR.get());
        generateAndWriteToDir(OutputDirectoryUtil.OUTPUT_DIR.get(), source);
      }
      else {
        logger.info("No output directory set. Attempting to discover target directory and write class file.");
        generateAndWriteToDiscoveredDirs(context, source);
      }
    }
    else {
      logger.info("Running in JUnit or Classic Dev Mode. Attempting to generate class in tmp directory {}", OUTPUT_TMP);
      final String tmpPath = new File(OUTPUT_TMP).getAbsolutePath();
      OutputDirectoryUtil.generateClassFileInTmpDir(SERIALIZER_PACKAGE_NAME, SERIALIZER_CLASS_NAME, source, tmpPath);
    }

    return source;
  }

  private void generateAndWriteToDiscoveredDirs(final GeneratorContext context, final String source) {
    OutputDirectoryUtil
      .generateClassFileInDiscoveredDirs(
              context,
              SERIALIZER_PACKAGE_NAME,
              SERIALIZER_CLASS_NAME,
              SOURCE_OUTPUT_TMP,
              source);
  }

  private void generateAndWriteToDir(final String classOutputDir, final String source) {
    ClassChangeUtil.generateClassFile(SERIALIZER_PACKAGE_NAME, SERIALIZER_CLASS_NAME, SOURCE_OUTPUT_TMP, source, classOutputDir);
  }

  @Override
  protected boolean isRelevantClass(final MetaClass clazz) {
    return clazz.isAnnotation() && clazz.unsafeIsAnnotationPresent(Qualifier.class);
  }

}
