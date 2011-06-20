package org.jboss.errai.cdi.rebind;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface EventProviderTestResult {

    public static final String EVENT_PROVIDER_FOR_STRING_WITHOUT_QUALIFIERS =
            "(javax.enterprise.event.Event<java.lang.String>) " +
                    " inj.provide(new Class[] {java.lang.String.class}, null)";

    public static final String EVENT_PROVIDER_FOR_STRING_WITH_QUALIFIERS =
            "(javax.enterprise.event.Event<java.lang.String>) " +
                    " inj.provide(new Class[] {java.lang.String.class}, new java.lang.annotation.Annotation[] {" +
                    "     new java.lang.annotation.Annotation() {" +
                    "         public Class<? extends java.lang.annotation.Annotation> annotationType() {" +
                    "             return org.jboss.errai.cdi.client.qualifier.B.class;" +
                    "         }" +
                    "     }," +
                    "     new java.lang.annotation.Annotation() {" +
                    "         public Class<? extends java.lang.annotation.Annotation> annotationType() {" +
                    "             return org.jboss.errai.cdi.client.qualifier.A.class;\n" +
                    "         }" +
                    "     }\n" +
                    "})";
}
