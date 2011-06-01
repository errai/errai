package org.jboss.errai.ioc.tests.rebind;

import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ObjectBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementBuilder;
import org.junit.Test;

import java.lang.annotation.Retention;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class ClassStructureBuilderTests extends AbstractStatementBuilderTest {
    @Test
    public void testOverrideConstructor() {

        String src = ObjectBuilder.newInstanceOf(Retention.class)
                .extend()
                .publicOverridesMethod("annotationType")
                .append(StatementBuilder.create().load("foo"))
                .append(StatementBuilder.create().load("bar"))
                .append(StatementBuilder.create().load("foobie"))
                .finish().toJavaString();

        System.out.println("src=" + src);
    }
}
