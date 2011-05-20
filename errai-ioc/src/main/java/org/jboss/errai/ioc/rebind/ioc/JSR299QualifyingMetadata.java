package org.jboss.errai.ioc.rebind.ioc;

import javax.enterprise.inject.Any;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock .
 */
public class JSR299QualifyingMetadata implements QualifyingMetadata {
    private Set<Annotation> qualifiers;
    private static Any ANY_INSTANCE = new Any() {
        public Class<? extends Annotation> annotationType() {
            return Any.class;
        }
    };

    public JSR299QualifyingMetadata(Set<Annotation> qualifiers) {
        this.qualifiers = qualifiers;
    }

    public boolean doesSatisfy(QualifyingMetadata metadata) {
        if (metadata instanceof JSR299QualifyingMetadata) {
            JSR299QualifyingMetadata comparable = (JSR299QualifyingMetadata) metadata;

            return ((comparable.qualifiers.size() == 1
                    && comparable.qualifiers.contains(ANY_INSTANCE))
                    || qualifiers.size() == 1
                    && qualifiers.contains(ANY_INSTANCE)
                    || comparable.qualifiers.containsAll(qualifiers));
        }
        return false;
    }

    public static JSR299QualifyingMetadata createFromAnnotations(Annotation[] annotations) {
        if (annotations == null) return createDefaultQualifyingMetaData();

        Set<Annotation> qualifiers = new HashSet<Annotation>();

        for (Annotation a : annotations) {
            if (a.annotationType().isAnnotationPresent(Qualifier.class)) {
                qualifiers.add(a);
            }
        }

        return qualifiers.isEmpty() ? null : new JSR299QualifyingMetadata(qualifiers);
    }

    public static JSR299QualifyingMetadata createDefaultQualifyingMetaData() {
        return new JSR299QualifyingMetadata(
                Collections.<Annotation>singleton(ANY_INSTANCE));
    }
}
