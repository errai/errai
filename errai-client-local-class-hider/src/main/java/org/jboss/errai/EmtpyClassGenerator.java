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
