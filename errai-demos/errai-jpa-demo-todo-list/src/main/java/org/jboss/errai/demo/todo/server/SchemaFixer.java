package org.jboss.errai.demo.todo.server;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@Startup @Singleton
public class SchemaFixer {

  @Inject EntityManager em;

  @PostConstruct
  public void alterSchemaIfNeeded() {
    System.out.println("Checking if schema needs update...");

    Query schemaCheck = em.createNativeQuery("SELECT 1 FROM information_schema.columns WHERE lower(table_name)='todolist_user' AND lower(column_name)='password'");
    if (schemaCheck.getResultList().isEmpty()) {
      System.out.println("Attempting to add password column...");
      Query ddlStmt = em.createNativeQuery("ALTER TABLE todolist_user ADD COLUMN password VARCHAR(100)");
      ddlStmt.executeUpdate();
    }
    else {
      System.out.println("Schema is up to date.");
    }
  }
}
