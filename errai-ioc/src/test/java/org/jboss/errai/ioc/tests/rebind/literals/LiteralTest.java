package org.jboss.errai.ioc.tests.rebind.literals;

import junit.framework.TestCase;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.values.LiteralFactory;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class LiteralTest extends TestCase {

    public void testIntegerLiteral() {
        assertEquals("1234", LiteralFactory.getLiteral(1234).generate());
    }

    public void testShortLiteral() {
        assertEquals("1234", LiteralFactory.getLiteral((short) 1234).generate());
    }

    public void testLongLiteral() {
        assertEquals("1234L", LiteralFactory.getLiteral(1234L).generate());
    }

    public void testDoubleLiteral() {
        assertEquals("1234.567d", LiteralFactory.getLiteral(1234.567d).generate());
    }

    public void testFloatLiteral() {
        assertEquals("1234.567f", LiteralFactory.getLiteral(1234.567f).generate());
    }

    public void testByteLiteral() {
        assertEquals("72", LiteralFactory.getLiteral((byte) 72).generate());
    }

    public void testBooleanLiteral() {
        assertEquals("false", LiteralFactory.getLiteral(false).generate());
    }

    public void testStringLiteral() {
        final String expected = "\"The quick brown fox said \\\"how do you do?\\\"\\nNew line.\\rCarriage Return!"
                + "\\t and a tab\"";

        final String input = "The quick brown fox said \"how do you do?\"\nNew line.\rCarriage Return!"
                + "\t and a tab";

        assertEquals(expected, LiteralFactory.getLiteral(input).generate());
    }

    public void testStringArrayCreation() {
        final String[][] input = new String[][]{{"Hello1", "Hello2"}, {"Hello3", "Hello4"}};
        final String expected = "new java.lang.String[][] {{\"Hello1\", \"Hello2\"}, {\"Hello3\", \"Hello4\"}}";

        assertEquals(expected, LiteralFactory.getLiteral(input).generate());
    }

    public void testClassLiteral() {
        assertEquals("java.lang.String.class", LiteralFactory.getLiteral(String.class).generate());
    }

}
