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

import static org.jboss.errai.enterprise.client.cdi.EventQualifierSerializer.SERIALIZER_CLASS_NAME;
import static org.jboss.errai.enterprise.client.cdi.EventQualifierSerializer.SERIALIZER_PACKAGE_NAME;

import java.io.File;

import javax.inject.Qualifier;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.rebind.AbstractAsyncGenerator;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.config.rebind.GenerateAsync;
import org.jboss.errai.enterprise.client.cdi.EventQualifierSerializer;
import org.jboss.errai.ioc.util.CDIAnnotationUtils;
import org.jboss.errai.marshalling.rebind.util.OutputDirectoryUtil;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@GenerateAsync(EventQualifierSerializer.class)
public class EventQualifierSerializerGenerator extends AbstractAsyncGenerator {

  private static final String OUTPUT_TMP = RebindUtils.getTempDirectory() + File.separator + "errai.cdi" + File.separator + "gen";

  @Override
  public String generate(final TreeLogger logger, final GeneratorContext context, final String typeName)
          throws UnableToCompleteException {
    return startAsyncGeneratorsAndWaitFor(EventQualifierSerializer.class, context, logger,
            SERIALIZER_PACKAGE_NAME, SERIALIZER_CLASS_NAME);
  }

  @Override
  protected String generate(final TreeLogger logger, final GeneratorContext context) {
    final String source = NonGwtEventQualifierSerializerGenerator.generateSource(CDIAnnotationUtils.getTranslatableQualifiers(context.getTypeOracle()));

    if (EnvUtil.isProdMode()) {
      OutputDirectoryUtil
        .generateClassFileInDiscoveredDirs(
                context,
                SERIALIZER_PACKAGE_NAME,
                SERIALIZER_CLASS_NAME,
                OUTPUT_TMP + File.separator + "event-qualifier-serializer",
                source);
    }
    else {
      final String tmpPath = new File(OUTPUT_TMP).getAbsolutePath();
      OutputDirectoryUtil.generateClassFileInTmpDir(SERIALIZER_PACKAGE_NAME, SERIALIZER_CLASS_NAME, source, tmpPath);
    }

    return source;
  }

  @Override
  protected boolean isRelevantClass(final MetaClass clazz) {
    return clazz.isAnnotation() && clazz.isAnnotationPresent(Qualifier.class);
  }

}
