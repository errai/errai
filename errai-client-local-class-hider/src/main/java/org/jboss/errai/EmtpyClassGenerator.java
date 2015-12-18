/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_5;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * A utility that generates an empty class: a trivial public subclass of
 * java.lang.Object which is in the expected package and has the expected name.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class EmtpyClassGenerator {

  private EmtpyClassGenerator() {
  };

  /**
   * Generate an empty class: a trivial public subclass of java.lang.Object
   * which is in the expected package and has the expected name.
   * 
   * @param className
   *          the fully qualified class name
   * @return byte array representing the empty class
   */
  public static byte[] generateEmptyClass(String className) {
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
