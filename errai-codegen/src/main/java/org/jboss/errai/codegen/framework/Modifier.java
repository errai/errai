package org.jboss.errai.codegen.framework;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public enum Modifier implements Comparable<Modifier> {
  Synchronized("synchronized"),
  Native("native"),
  JSNI("native"),
  Static("static"),
  Final("final"),
  Abstract("abstract"),
  Transient("transient"),
  Volatile("volatile");

  private final String canonicalString;

  Modifier(String cananonicalString) {
    this.canonicalString = cananonicalString;
  }

  public String getCanonicalString() {
    return canonicalString;
  }
}
