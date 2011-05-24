package org.jboss.errai.ioc.tests.rebind;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.JavaReflectionClass;
import org.junit.Test;

public class StatementBuilderTest {

    @Test
    public void testLoop() throws Exception {
        Statement createObject = StatementBuilder.create().newObject(
                new JavaReflectionClass(Class.forName("java.lang.Integer")));

        Statement createAnotherObject = StatementBuilder.create().newObject(
                new JavaReflectionClass(Class.forName("java.lang.Integer")));

        Statement loop = StatementBuilder.create()
                .loadVariable("element", new JavaReflectionClass(Class.forName("java.lang.Integer")))
                .loadVariable("list", new JavaReflectionClass(Class.forName("java.util.List")))
                .loop("element", "list")
                .addStatement(createObject)
                .addStatement(createAnotherObject);

        System.out.println(loop.getStatement());
    }
    
    @Test
    public void testNestedLoops() throws Exception {
        Statement createObject = StatementBuilder.create().newObject(
                new JavaReflectionClass(Class.forName("java.lang.Integer")));

        Statement loop = StatementBuilder.create()
                .loadVariable("element", new JavaReflectionClass(Class.forName("java.lang.Integer")))
                .loadVariable("list", new JavaReflectionClass(Class.forName("java.util.List")))
                .loop("element", "list")
                .addStatement(createObject);

        Statement loop2 = StatementBuilder.createInScopeOf(loop)
                .loadVariable("element2", new JavaReflectionClass(Class.forName("java.lang.Integer")))
                .loop("element2", "list")
                .addStatement(loop);

        System.out.println(loop2.getStatement());
    }
}
