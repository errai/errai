package org.jboss.errai.ioc.tests.rebind;

import static org.junit.Assert.fail;

import org.jboss.errai.ioc.client.api.builtin.MessageBusProvider;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedMethodException;
import org.junit.Test;

/**
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class StatementBuilderTest {

    @Test
    public void testInvoke() {
        try {
            StatementBuilder.create()
                    .loadVariable("injector", MessageBusProvider.class)
                    .invoke("provide", Variable.get("param", String.class), Variable.get("param2", Integer.class));
            fail("expected UndefinedMethodException");
        } catch(UndefinedMethodException udme) {
            //expected
        }
        
        Statement invokeStatement = StatementBuilder.create()
            .loadVariable("injector", MessageBusProvider.class)
            .invoke("provide");
        
        System.out.println(invokeStatement.generate());
        
        Statement invokeStatement2 = StatementBuilder.create()
            .loadVariable("s", String.class)
            .invoke("replaceAll", Variable.get("regex", String.class), Variable.get("replacement", String.class));
    
        System.out.println(invokeStatement2.generate());
    }
}
