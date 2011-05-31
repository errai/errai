package org.jboss.errai.ioc.tests.rebind;

import java.lang.annotation.Target;

import javax.annotation.PostConstruct;

import org.jboss.errai.ioc.rebind.ioc.codegen.AnnotationEncoder;
import org.junit.Test;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class AnnotationEncoderTest extends AbstractStatementBuilderTest {
    
    @Test
    public void testEncode() {
        String enc = AnnotationEncoder.encode(PostConstruct.class.getAnnotation(Target.class));

        assertEquals("new java.lang.annotation.Target() {\n" +
        		        "public java.lang.Class annotationType() {\n" +
                            "return java.lang.annotation.Target.class;\n" +
                        "}\n" +
                    "}\n", enc);
    }
}