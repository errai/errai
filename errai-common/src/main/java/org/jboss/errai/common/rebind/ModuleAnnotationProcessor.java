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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

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

      final Set<String> currentElements = new LinkedHashSet<String>(allKnownElements.size());
      Writer writer = null;
      try {
        FileObject fo = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "classlist.mf");

        BufferedReader reader = null;
        try {
          reader = new BufferedReader(fo.openReader(false));
          String line;
          while ((line = reader.readLine()) != null) {
            currentElements.add(line);
          }
        } catch (Exception e) {
          // assume file didn't exist (no way to check this using the FileObject interface,
          // and it throws an UNCHECKED EXCEPTION if you try to open a reader for a nonexistant file)
        } finally {
          closeQuietly(reader);
        }

        boolean hasContentChanged = false;
        for (final Element e : allKnownElements) {
          if (currentElements.add(e.toString())) {
            hasContentChanged = true;
          }
        }

        if (hasContentChanged) {
          StringBuilder newContent = new StringBuilder();
          for(String e : currentElements) {
            newContent.append(e).append('\n');
          }
          writer = fo.openWriter();
          writer.write(newContent.toString());
        }

      } catch (IOException e) {
        e.printStackTrace();

      } finally {
        closeQuietly(writer);
      }
    }
    return false;
  }

  private void closeQuietly(Closeable closeable) {
    if (closeable !=null) {
      try {
        closeable.close();
      } catch (Exception e) {
        //ignore
      }
    }
  }
}
