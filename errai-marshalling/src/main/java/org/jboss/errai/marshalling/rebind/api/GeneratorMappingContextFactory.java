/*
 * Copyright 2014 JBoss, by Red Hat, Inc
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

package org.jboss.errai.marshalling.rebind.api;

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.marshalling.rebind.MarshallerGeneratorFactory;
import org.jboss.errai.marshalling.rebind.MarshallerOutputTarget;

/**
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class GeneratorMappingContextFactory {
  private static final Map<MarshallerOutputTarget, GeneratorMappingContext> contexts =
      new HashMap<MarshallerOutputTarget, GeneratorMappingContext>();

  public static GeneratorMappingContext getFor(MarshallerOutputTarget target) {
    GeneratorMappingContext context = contexts.get(target);
    if (context == null) {
      throw new GenerationException("Generation context for output target " + target + " was not created!");
    }
    
    return context;
  }

  public static GeneratorMappingContext create(final MarshallerOutputTarget target,
      final MarshallerGeneratorFactory marshallerGeneratorFactory,
      final ClassStructureBuilder<?> classStructureBuilder,
      final ArrayMarshallerCallback arrayMarshallerCallback) {

    GeneratorMappingContext context =
        new GeneratorMappingContext(marshallerGeneratorFactory, classStructureBuilder, arrayMarshallerCallback);

    contexts.put(target, context);

    return context;
  }
}
