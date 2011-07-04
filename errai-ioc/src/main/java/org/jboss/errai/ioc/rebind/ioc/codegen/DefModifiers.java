package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.builder.Builder;

import java.util.*;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class DefModifiers implements Builder {
  private Set<Modifier> modifiers = new HashSet<Modifier>();

  public DefModifiers() {
  }

  public DefModifiers(Modifier... modifiers) {
    addModifiers(modifiers);
  }

  public DefModifiers addModifiers(Modifier... modifier) {
    modifiers.addAll(Arrays.asList(modifier));
    return this;
  }


  @Override
  public String toJavaString() {
    StringBuilder sbuf = new StringBuilder();

    if (modifiers.contains(Modifier.Synchronized)) {
      sbuf.append("synchronized ");
    }

    if (modifiers.contains(Modifier.Transient)) {
      sbuf.append("transient ");
    }

    if (modifiers.contains(Modifier.Abstract)) {
      sbuf.append("abstract ");
    }

    if (modifiers.contains(Modifier.Static)) {
      sbuf.append("static ");
    }

    if (modifiers.contains(Modifier.Final)) {
      sbuf.append("final ");
    }

    return sbuf.toString().trim();
  }
}
