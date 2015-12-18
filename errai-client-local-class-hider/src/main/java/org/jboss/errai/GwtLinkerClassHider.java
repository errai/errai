/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A class file "transformer" that overrides GWT linker classes with classes
 * that are completely empty.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class GwtLinkerClassHider implements ClassFileTransformer {
  private final boolean debug;

  public GwtLinkerClassHider(boolean debug) {
    this.debug = debug;
  }

  /**
   * Generates a class with a no-args public constructor.
   * 
   * @param loader
   *          ignored.
   * @param className
   *          The generated class has this package and name.
   * @param classBeingRedefined
   *          ignored.
   * @param protectionDomain
   *          ignored.
   * @param classfileBuffer
   *          ignored.
   * @return bytecode for an empty class whose package and class name are
   *         determined by the {@code className} parameter.
   */
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
          ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

    if (className.contains("Linker")) {
      final List<String> annotations = new ArrayList<String>();
      ClassReader cr = new ClassReader(classfileBuffer);
      ClassVisitor cv = new ClassVisitor(Opcodes.ASM4) {
        @Override
        public AnnotationVisitor visitAnnotation(String name, boolean val) {
          annotations.add(name);
          return super.visitAnnotation(name, val);
        }

      };
      cr.accept(cv, 0);

      if (annotations.contains("Lcom/google/gwt/core/ext/linker/Shardable;")
              || annotations.contains("Lcom/google/gwt/core/ext/linker/LinkerOrder;")) {

        if (debug) {
          System.out.println("client-local-class-hider (linkers): hiding GWT linker class: " + className);
        }
        return EmtpyClassGenerator.generateEmptyClass(className);
      }
    }

    if (debug) {
      System.out.println("client-local-class-hider (linkers): NOT hiding class (not a linker): " + className);
    }
    return null;
  }
}
