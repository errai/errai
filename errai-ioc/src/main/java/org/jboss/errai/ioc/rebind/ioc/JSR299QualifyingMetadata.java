package org.jboss.errai.ioc.rebind.ioc;

import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock .
 */
public class JSR299QualifyingMetadata implements QualifyingMetadata {
    private Set<Annotation> qualifiers;

    public JSR299QualifyingMetadata(Set<Annotation> qualifiers) {
        this.qualifiers = qualifiers;
    }

    public boolean doesSatisfy(QualifyingMetadata metadata) {
        return metadata instanceof JSR299QualifyingMetadata
                && qualifiers.containsAll(((JSR299QualifyingMetadata) metadata).qualifiers);
    }

    public static JSR299QualifyingMetadata createFromAnnotations(Annotation[] annotations) {
        if (annotations == null) return null;

        Set<Annotation> qualifiers = new HashSet<Annotation>();

        for (Annotation a : annotations) {
            if (a.annotationType().isAnnotationPresent(Qualifier.class)) {
                qualifiers.add(a);
            }
        }

        return qualifiers.isEmpty() ? null : new JSR299QualifyingMetadata(qualifiers);
    }
}
