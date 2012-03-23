package org.jboss.errai.codegen.framework.tests;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.SnapshotMaker;
import org.jboss.errai.codegen.framework.tests.model.Person;
import org.jboss.errai.codegen.framework.tests.model.PersonImpl;
import org.junit.Assert;
import org.junit.Test;

public class SnapshotMakerTest {

  @Test
  public void testGenerateSnapshotOfMethod() throws Exception {
    Person mother = new PersonImpl("mom", 30, null);
    Person child = new PersonImpl("kid", 5, mother);
    System.out.println(SnapshotMaker.makeSnapshotAsSubclass(child, Person.class, Person.class).generate(Context.create()));
  }

  // FIXME this test is failing now, but Mike wants me to push anyway
  @Test
  public void testNoStackOverflowOnObjectCycle() {
    PersonImpl cycle1 = new PersonImpl("cycle1", 30, null);
    Person cycle2 = new PersonImpl("cycle2", 5, cycle1);
    cycle1.setMother(cycle2);

    try {
      System.out.println(SnapshotMaker.makeSnapshotAsSubclass(cycle2, Person.class, Person.class).generate(Context.create()));
      Assert.fail("Instance cycle was not detected");
    } catch (UnsupportedOperationException e) {
      // expected
    }
  }
}
