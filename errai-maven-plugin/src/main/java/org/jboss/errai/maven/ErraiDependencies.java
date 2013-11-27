/*
 * Copyright 2013 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.maven;

import java.util.ArrayList;
import java.util.List;

public class ErraiDependencies {
  private ErraiDependencies() {};

  // A map of group ids to a list of artifact ids that should be treated as provided (and
  // therefore not be packaged and deployed to the server with an Errai app)

  // Java collection literals FTW?
  @SuppressWarnings("serial")
  public static List<String> providedDependecies = new ArrayList<String>() {{
    add("com.google.guava:guava-gwt");
    add("hsqldb:hsqldb");
    add("javax.annotation:jsr250-api");
    add("javax.enterprise:cdi-api");
    add("javax.inject:javax.inject");
    add("javax.validation:validation-api");
    add("junit:junit");
    add("org.hibernate.common:hibernate-commons-annotations");
    add("org.hibernate.javax.persistence:hibernate-jpa-2.0-api");
    add("org.hibernate:hibernate-core");
    add("org.hibernate:hibernate-entitymanager");
    add("org.hibernate:hibernate-validator");
    add("org.jboss.logging:jboss-logging");
    add("org.jboss.resteasy:jaxrs-api");
    add("org.jboss.spec.javax.interceptor:jboss-interceptors-api_1.1_spec");
    add("org.jboss.spec.javax.transaction:jboss-transaction-api_1.1_spec");
    add("org.jboss.weld.servlet:weld-servlet-core");
    add("org.jboss.weld:weld-core");
    add("org.jboss.weld:weld-api");
    add("org.jboss.weld:weld-spi");
    add("xml-apis:xml-apis");
    add("org.jboss.errai.io.netty:netty");
    add("org.mortbay.jetty:jetty-naming");
    add("org.jboss.errai:errai-cdi-jetty");
    add("org.jboss.errai:errai-cdi-jboss");
    add("org.jboss.errai:errai-client-local-class-hider");
    add("org.jboss.errai:errai-codegen-gwt");
    add("org.jboss.errai:errai-data-binding");
    add("org.jboss.errai:errai-javax-enterprise");
    add("org.jboss.errai:errai-jaxrs-client");
    add("org.jboss.errai:errai-jpa-client");
    add("org.jboss.errai:errai-navigation");
    add("org.jboss.errai:errai-tools");
    add("org.jboss.errai:errai-ui");
    add("org.jboss.aesh:aesh");
    add("org.hibernate:antlr");
    add("org.ow2.asm:asm");
    add("dom4j:dom4j");
    add("org.fusesource.jansi:jansi");
    add("geronimo-spec:geronimo-spec-jta");
    add("org.jboss.as:jboss-as-build-config");
    add("org.jboss.as:jboss-as-cli");
    add("org.jboss.as:jboss-as-controller-client");
    add("org.jboss.as:jboss-as-protocol");
    add("org.jboss:jboss-dmr");
    add("org.jboss.logging:jboss-logging-processor");
    add("org.jboss.logmanager:jboss-logmanager");
    add("org.jboss.marshalling:jboss-marshalling");
    add("org.jboss.marshalling:jboss-marshalling-river");
    add("org.jboss.remoting3:jboss-remoting");
    add("org.jboss.sasl:jboss-sasl");
    add("org.jboss.threads:jboss-threads");
    add("org.jboss:jboss-vfs");
    add("org.jboss.jdeparser:jdeparser");
    add("org.mortbay.jetty:jetty-plus");
    add("log4j:log4j");
    add("org.jboss.remotingjmx:remoting-jmx");
    add("org.jboss.xnio:xnio-api");
    add("org.jboss.xnio:xnio-nio");
  }};

}