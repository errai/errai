package org.jboss.errai.ioc.tests.rebind;

import java.lang.annotation.Target;

import javax.annotation.PostConstruct;

import junit.framework.TestCase;

import org.jboss.errai.ioc.rebind.ioc.codegen.AnnotationEncoder;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class AnnotationEncoderTest extends TestCase {
    public void testEncode() {
        String enc = AnnotationEncoder.encode(PostConstruct.class.getAnnotation(Target.class));

        System.out.println(enc);
    }
}
