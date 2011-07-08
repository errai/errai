package org.jboss.errai.cdi.rebind;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface ObservesExtensionTestResult {

  public static final String OBSERVES_EXTENSION_WITHOUT_QUALIFIERS = ""
            + "bus.subscribe(\"cdi.event:org.jboss.errai.cdi.client.events.BusReadyEvent\", "
            + "   new org.jboss.errai.bus.client.api.MessageCallback() {"
            + "       public void callback(org.jboss.errai.bus.client.api.Message message) {"
            + "           java.util.Set<String> methodQualifiers = new java.util.HashSet<String>();"
            + "           java.util.Set<String> qualifiers ="
            + "               message.get(java.util.Set.class,org.jboss.errai.cdi.client.CDIProtocol.QUALIFIERS);"
            + "           if(methodQualifiers.equals(qualifiers) || (qualifiers==null && methodQualifiers.isEmpty())) {"
            + "               java.lang.Object response = "
            + "                   message.get(org.jboss.errai.cdi.client.events.BusReadyEvent.class, "
            + "                               org.jboss.errai.cdi.client.CDIProtocol.OBJECT_REF); "
            + "               inj.withoutQualifiers((org.jboss.errai.cdi.client.events.BusReadyEvent) response);"
            + "           }"
            + "       }"
            + "   });";

  public static final String OBSERVES_EXTENSION_WITH_QUALIFIERS = ""
            + "bus.subscribe(\"cdi.event:org.jboss.errai.cdi.client.events.BusReadyEvent\", "
            + "   new org.jboss.errai.bus.client.api.MessageCallback() {"
            + "       public void callback(org.jboss.errai.bus.client.api.Message message) {"
            + "           java.util.Set<String> methodQualifiers = new java.util.HashSet<String>();"
            + "           methodQualifiers.add(\"org.jboss.errai.cdi.client.qualifier.B\");"
            + "           methodQualifiers.add(\"org.jboss.errai.cdi.client.qualifier.A\");"
            + "           java.util.Set<String> qualifiers ="
            + "               message.get(java.util.Set.class,org.jboss.errai.cdi.client.CDIProtocol.QUALIFIERS);"
            + "           if(methodQualifiers.equals(qualifiers) || (qualifiers==null && methodQualifiers.isEmpty())) {"
            + "               java.lang.Object response = "
            + "                   message.get(org.jboss.errai.cdi.client.events.BusReadyEvent.class, "
            + "                               org.jboss.errai.cdi.client.CDIProtocol.OBJECT_REF); "
            + "               inj.withQualifiers((org.jboss.errai.cdi.client.events.BusReadyEvent) response);"
            + "           }"
            + "       }"
            + "   });";
}
