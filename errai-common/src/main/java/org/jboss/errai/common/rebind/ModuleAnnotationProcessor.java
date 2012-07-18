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

package org.jboss.errai.common.rebind;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("*")
public class ModuleAnnotationProcessor extends AbstractProcessor {

  final Set<Element> allKnownElements = new HashSet<Element>();

  @SuppressWarnings("NullArgumentToVariableArgMethod")
  @Override
  public boolean process(final Set<? extends TypeElement> typeElements,
                         final RoundEnvironment roundEnvironment) {

    allKnownElements.addAll(roundEnvironment.getRootElements());

    if (roundEnvironment.processingOver()) {
      final StringBuilder builder = new StringBuilder();
      for (final Element e : allKnownElements) {
        builder.append(e.toString()).append('\n');
      }

      try {
        final FileObject fo
                = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "classlist.mf", null);

        final Writer writer = fo.openWriter();
        writer.write(builder.toString());
        writer.close();
      }
      catch (IOException e) {
        e.printStackTrace();
      }

    }
    return false;
  }
}
