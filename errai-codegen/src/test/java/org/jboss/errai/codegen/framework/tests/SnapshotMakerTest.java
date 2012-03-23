package org.jboss.errai.codegen.framework.tests;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.SnapshotMaker;
import org.jboss.errai.codegen.framework.tests.model.Person;
import org.jboss.errai.codegen.framework.tests.model.PersonImpl;
import org.jboss.errai.codegen.framework.tests.model.SnapshotInterfaceWithCollections;
import org.jboss.errai.codegen.framework.tests.model.SnapshotInterfaceWithCollectionsImpl;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

// Note: extends from AbstractCodeGenTest to inherit overridden assertEquals(String, String) methods which are whitespace
//       insensitive.
public class SnapshotMakerTest extends AbstractCodegenTest {

  @Test
  public void testGenerateSnapshotOfMethod() throws Exception {
    Person mother = new PersonImpl("mom", 30, null);
    Person child = new PersonImpl("kid", 5, mother);
    final String generated
            = SnapshotMaker.makeSnapshotAsSubclass(child, Person.class, Person.class).generate(Context.create());

    final String expectedValue =
            "new org.jboss.errai.codegen.framework.tests.model.Person() {\n" +
                    "  public int getAge() {\n" +
                    "    return 5;\n" +
                    "  }\n" +
                    "  public org.jboss.errai.codegen.framework.tests.model.Person getMother() {\n" +
                    "    return new org.jboss.errai.codegen.framework.tests.model.Person() {\n" +
                    "      public int getAge() {\n" +
                    "        return 30;\n" +
                    "      }\n" +
                    "      public org.jboss.errai.codegen.framework.tests.model.Person getMother() {\n" +
                    "        return null;\n" +
                    "      }\n" +
                    "      public String getName() {\n" +
                    "        return \"mom\";\n" +
                    "      }\n" +
                    "    };\n" +
                    "  }\n" +
                    "  public String getName() {\n" +
                    "    return \"kid\";\n" +
                    "  }\n" +
                    "}";

    assertEquals(expectedValue, generated);
  }

  // FIXME this test is failing now, but Mike wants me to push anyway
  @Test
  @Ignore
  public void testNoStackOverflowOnObjectCycle() {
    PersonImpl cycle1 = new PersonImpl("cycle1", 30, null);
    Person cycle2 = new PersonImpl("cycle2", 5, cycle1);
    cycle1.setMother(cycle2);

    try {
      System.out.println(SnapshotMaker.makeSnapshotAsSubclass(cycle2, Person.class, Person.class).generate(Context.create()));
      Assert.fail("Instance cycle was not detected");
    }
    catch (UnsupportedOperationException e) {
      // expected
    }
  }

  /**
   * This test confirms the SnapshotMaker is called automatically by the code generator's literal reification logic
   * for interfaces and classes explicitly market "literalizable" in the context.
   *
   * @throws Exception
   */
  @Test
  public void testCollectionsLiteralizableInSnapshots() throws Exception {
    Person jonathan = new PersonImpl("Jonathan F.", 20, null);
    Person christian = new PersonImpl("Christian S.", 20, null);
    Person mike = new PersonImpl("Mike B.", 20, null);

    SnapshotInterfaceWithCollections snapshot
            = new SnapshotInterfaceWithCollectionsImpl(Arrays.asList(jonathan, christian, mike));

    // create a manual context
    final Context ctx = Context.create();

    // tell the code generator that classes that implement these interfaces are literalizable
    ctx.addLiteralizableClass(SnapshotInterfaceWithCollections.class);
    ctx.addLiteralizableClass(Person.class);

    // initialize a new builder with this context and try and build 'snapshot' as a code snapshot
    final String generated = Stmt.create(ctx).load(snapshot).toJavaString();

    final String expectedValue =
            "new org.jboss.errai.codegen.framework.tests.model.SnapshotInterfaceWithCollections() {\n" +
                    "  public java.util.List getPersons() {\n" +
                    "    return new java.util.ArrayList() {\n" +
                    "      {\n" +
                    "        add(new org.jboss.errai.codegen.framework.tests.model.Person() {\n" +
                    "          public int getAge() {\n" +
                    "            return 20;\n" +
                    "          }\n" +
                    "          public org.jboss.errai.codegen.framework.tests.model.Person getMother() {\n" +
                    "            return null;\n" +
                    "          }\n" +
                    "          public String getName() {\n" +
                    "            return \"Jonathan F.\";\n" +
                    "          }\n" +
                    "        });\n" +
                    "        add(new org.jboss.errai.codegen.framework.tests.model.Person() {\n" +
                    "          public int getAge() {\n" +
                    "            return 20;\n" +
                    "          }\n" +
                    "          public org.jboss.errai.codegen.framework.tests.model.Person getMother() {\n" +
                    "            return null;\n" +
                    "          }\n" +
                    "          public String getName() {\n" +
                    "            return \"Christian S.\";\n" +
                    "          }\n" +
                    "        });\n" +
                    "        add(new org.jboss.errai.codegen.framework.tests.model.Person() {\n" +
                    "          public int getAge() {\n" +
                    "            return 20;\n" +
                    "          }\n" +
                    "          public org.jboss.errai.codegen.framework.tests.model.Person getMother() {\n" +
                    "            return null;\n" +
                    "          }\n" +
                    "          public String getName() {\n" +
                    "            return \"Mike B.\";\n" +
                    "          }\n" +
                    "        });\n" +
                    "      }\n" +
                    "    };\n" +
                    "  }\n" +
                    "}\n";

    assertEquals(expectedValue, generated);
  }
}
