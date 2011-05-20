package org.jboss.errai.ioc.tests.rebind;

import junit.framework.TestCase;
import org.jboss.errai.ioc.rebind.ioc.codegen.AnnotationEncoder;

import javax.annotation.PostConstruct;
import java.lang.annotation.Target;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class AnnotationEncoderTests extends TestCase {
    public void testEncode() {
        String enc = AnnotationEncoder.encode(PostConstruct.class.getAnnotation(Target.class));

        System.out.println(enc);
    }
}
