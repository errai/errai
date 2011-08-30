package org.jboss.errai.codegen.framework;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.errai.codegen.framework.builder.Builder;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class DefModifiers implements Builder {
  private Set<Modifier> modifiers = new TreeSet<Modifier>();

  public DefModifiers() {
  }

  public DefModifiers(Modifier... modifiers) {
    addModifiers(modifiers);
  }

  public static DefModifiers none() {
    return new DefModifiers();
  }
  
  public DefModifiers addModifiers(Modifier... modifier) {
    modifiers.addAll(Arrays.asList(modifier));
    return this;
  }

  public boolean hasModifier(Modifier modifier) {
    return modifiers.contains(modifier);
  }

  @Override
  public String toJavaString() {
    StringBuilder sbuf = new StringBuilder();

    for (Modifier m : modifiers) {
      sbuf.append(m.getCanonicalString()).append(" ");
    }

    return sbuf.toString().trim();
  }
}
