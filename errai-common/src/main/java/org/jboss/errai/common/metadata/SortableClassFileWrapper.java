package org.jboss.errai.common.metadata;

import javassist.bytecode.ClassFile;

/**
* @author Mike Brock
*/
class SortableClassFileWrapper implements Comparable<SortableClassFileWrapper> {
  private String name;
  private ClassFile classFile;

  SortableClassFileWrapper(final String name, final ClassFile classFile) {
    this.name = name;
    this.classFile = classFile;
  }

  public ClassFile getClassFile() {
    return classFile;
  }

  @Override
  public int compareTo(final SortableClassFileWrapper o) {
    return name.compareTo(o.name);
  }
}
