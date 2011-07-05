package org.jboss.errai.ioc.rebind.ioc.codegen;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public enum Modifier implements Comparable<Modifier> {
  Synchronized("synchronized"),
  Native("native"),
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
