package org.jboss.errai.ioc.tests.rebind;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.LoopBodyBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ContextBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.OutOfScopeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.TypeNotIterableException;
import org.junit.Test;

import javax.enterprise.util.TypeLiteral;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;

/**
 * Tests the generation of loops using the {@link org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.StatementBuilder} API.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class LoopBuilderTest extends AbstractStatementBuilderTest implements LoopBuilderTestResult {

    @Test
    public void testForeachLoop() throws Exception {
        Statement createObject = StatementBuilder.create()
                .newObject(String.class);

        Statement createAnotherObject = StatementBuilder.create()
                .newObject(Object.class);

        String foreachWithListOfStrings = StatementBuilder.create()
                .addVariable("list", new TypeLiteral<List<String>>() {
                })
                .loadVariable("list")
                .foreach("element")
                .generate(Context.create());

        String foreachWithStringArray = StatementBuilder.create()
                .addVariable("list", String[].class)
                .loadVariable("list")
                .foreach("element")
                .execute(createObject)
                .generate(Context.create());

        String foreachWithList = StatementBuilder.create()
                .addVariable("list", List.class)
                .loadVariable("list")
                .foreach("element")
                .execute(createObject)
                .execute(createAnotherObject)
                .generate(Context.create());

        assertEquals("failed to generate foreach loop using a List<String>",
                FOREACH_RESULT_STRING_IN_LIST, foreachWithListOfStrings);
        assertEquals("failed to generate foreach loop using a String[]",
                FOREACH_RESULT_STRING_IN_ARRAY_ONE_STATEMENT, foreachWithStringArray);
        assertEquals("failed to generate foreach loop using a List<?>",
                FOREACH_RESULT_OBJECT_IN_LIST_TWO_STATEMENTS, foreachWithList);
    }

    @Test
    public void testForeachLoopWithUndefinedCollection() throws Exception {
        try {
            StatementBuilder.create()
                    .loadVariable("list")
                    .foreach("element", Integer.class)
                    .generate(Context.create());

            fail("Expected OutOfScopeException");
        } catch (OutOfScopeException oose) {
            // expected
        }
    }

    @Test
    public void testForeachLoopWithProvidedLoopVarType() throws Exception {
        Statement loop = StatementBuilder.create()
                .addVariable("list", new TypeLiteral<List<String>>() {
                })
                .loadVariable("list")
                .foreach("element", Object.class);

        assertEquals("failed to generate foreach loop with provided loop var type",
                FOREACH_RESULT_OBJECT_IN_LIST, loop.generate(Context.create()));

        try {
            StatementBuilder.create()
                    .addVariable("list", new TypeLiteral<List<String>>() {
                    })
                    .loadVariable("list")
                    .foreach("element", Integer.class)
                    .generate(Context.create());

            fail("Expected InvalidTypeException");
        } catch (InvalidTypeException ite) {
            // expected
        }
    }

    @Test
    public void testNestedForeachLoops() throws Exception {
        Statement createObject = StatementBuilder.create().newObject(String.class);

        Statement outerLoop = StatementBuilder.create()
                .addVariable("list", new TypeLiteral<List<String>>() {
                })
                .loadVariable("list")
                .foreach("element")
                .execute(StatementBuilder.create(
                        ContextBuilder.create().addVariable(Variable.create("anotherList", new TypeLiteral<List<String>>() {
                        })))
                        .loadVariable("anotherList")
                        .foreach("anotherElement")
                        .execute(createObject)
                );

        assertEquals("failed to generate nested foreach loops",
                FOREACH_RESULT_NESTED_STRING_IN_LIST, outerLoop.generate(Context.create()));
    }

    @Test
    public void testForeachLoopWithInvalidCollectionType() throws Exception {

        try {
            StatementBuilder.create()
                    .addVariable("list", String.class)
                    .loadVariable("list")
                    .foreach("element")
                    .generate(Context.create());

            fail("Expected TypeNotIterableException");
        } catch (TypeNotIterableException tnie) {
            // expected
        }
    }

    @Test
    public void testForeachLoopWithInvoke() throws Exception {
        Statement loop = StatementBuilder.create()
                .addVariable("map", Map.class)
                .loadVariable("map")
                .invoke("keySet")
                .foreach("key");

        assertEquals("failed to generate foreach loop using invoke()",
                FOREACH_RESULT_KEYSET_LOOP, loop.generate(Context.create()));
    }

    @Test
    public void testForeachLoopWithLiterals() throws Exception {
        LoopBodyBuilder loopBody = StatementBuilder.create()
                .loadLiteral(new String[]{"s1", "s2"})
                .foreach("s");

        Statement body = StatementBuilder.create(loopBody.getContext()).loadVariable("s").invoke("getBytes");
        Statement loop = loopBody.execute(body);
        assertEquals("failed to generate foreach loop using a literal String array",
                FOREACH_RESULT_LITERAL_STRING_ARRAY, loop.generate(Context.create()));

        Context c = ContextBuilder.create().addVariable(Variable.create("s", String.class)).getContext();
        loop = StatementBuilder.create(c)
                .loadLiteral(new String[]{"s1", "s2"})
                .foreach("s")
                .execute(StatementBuilder.create(c).loadVariable("s").invoke("getBytes"));

        assertEquals("failed to generate foreach loop using a literal String array",
                FOREACH_RESULT_LITERAL_STRING_ARRAY, loop.generate(c));
    }
}
