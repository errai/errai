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

package org.jboss.errai.marshalling.server.util;

import org.jboss.errai.codegen.util.ClassChangeUtil;
import org.jboss.errai.config.ErraiConfiguration;
import org.jboss.errai.marshalling.client.api.MarshallerFactory;
import org.jboss.errai.marshalling.rebind.MarshallerGeneratorFactory;
import org.jboss.errai.marshalling.rebind.MarshallerOutputTarget;
import org.jboss.errai.marshalling.rebind.MarshallersGenerator;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;
import org.slf4j.Logger;

import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Utility which provides convenience methods for generating marshallers for the server-side.
 *
 * @author Mike Brock
 */
public abstract class ServerMarshallUtil {
  private static Logger log = getLogger("ErraiMarshalling");

  @SuppressWarnings("unchecked")
  public static Class<? extends MarshallerFactory> getGeneratedMarshallerFactoryForServer(final ErraiConfiguration erraiConfiguration) {
    final String packageName = MarshallersGenerator.SERVER_PACKAGE_NAME;
    final String simpleClassName = MarshallersGenerator.SERVER_CLASS_NAME;
    final String fullyQualifiedClassName = packageName + "." + simpleClassName;

    final Optional<Class<?>> generatedMarshaller = ClassChangeUtil.loadClassIfPresent(packageName, simpleClassName);

    if (generatedMarshaller.isPresent()) {
      return (Class<? extends MarshallerFactory>) generatedMarshaller.get();
    }
    else if (!MarshallingGenUtil.isForceStaticMarshallers(erraiConfiguration)) {
      return null;
    }
    else {
      log.info("couldn't find {} class, attempting to generate ...", fullyQualifiedClassName);

      final String classStr = MarshallerGeneratorFactory.getFor(null, MarshallerOutputTarget.Java, erraiConfiguration)
              .generate(packageName, simpleClassName);

      return (Class<? extends MarshallerFactory>) ClassChangeUtil.compileAndLoadFromSource(packageName, simpleClassName, classStr);
    }
  }
}
