package org.jboss.errai.jpa.client.local.backend;

public interface EntryVisitor {
  public void visit(String key, String value);
}