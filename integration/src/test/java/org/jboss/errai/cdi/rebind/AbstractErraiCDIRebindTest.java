package org.jboss.errai.cdi.rebind;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractErraiCDIRebindTest {
    
    protected static void assertEquals(String message, String expected, String actual) {
        org.junit.Assert.assertEquals(message, expected.replaceAll("\\s+", " ").trim(), 
                actual.replaceAll("\\s+", " ").trim());
    }
}
