/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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

package org.jboss.errai;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_5;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * A class file "transformer" that ignores the original class definition,
 * instead generating a class that is completely boring: a trivial public
 * subclass of java.lang.Object which is in the expected package and has the
 * expected name.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class BoringClassGenerator implements ClassFileTransformer {

  private final Pattern hiddenClassNamePattern;
  private final boolean debug;

  public BoringClassGenerator(Pattern hiddenClassNamePattern, boolean debug) {
    this.hiddenClassNamePattern = hiddenClassNamePattern;
    this.debug = debug;

    if (debug) {
      System.out.println("client-local-class-hider: hiding classes that match the regular expression: " + hiddenClassNamePattern);
    }
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
  public byte[] transform(ClassLoader loader, String className,
      Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
      byte[] classfileBuffer) throws IllegalClassFormatException {

    if (!hiddenClassNamePattern.matcher(className).matches()) {
      if (debug) {
        System.out.println("client-local-class-hider: not hiding " + className);
      }
      return null;
    }

    if (debug) {
      System.out.println("client-local-class-hider: hiding " + className);
    }

    ClassWriter cw = new ClassWriter(0);
    MethodVisitor mv;

    cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);

    cw.visitSource("FakeClass__GENERATED_BY_JBoss_ClientLocalClassHider.java", null);

    {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(15, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      mv.visitInsn(RETURN);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLocalVariable("this", "L" + className + ";", null, l0, l1, 0);
      mv.visitMaxs(1, 1);
      mv.visitEnd();
    }

    cw.visitEnd();

    return cw.toByteArray();
  }
}
