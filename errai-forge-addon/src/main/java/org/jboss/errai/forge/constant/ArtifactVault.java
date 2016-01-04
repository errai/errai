/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.forge.constant;

import org.jboss.errai.forge.facet.base.AbstractBaseFacet;
import org.jboss.forge.addon.dependencies.Dependency;

import java.util.*;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public final class ArtifactVault {

  public static final String ERRAI_GROUP_ID = "org.jboss.errai";

  /**
   * An enumeration of Maven dependency artifacts used by various facets.
   * 
   * @author Max Barkley <mbarkley@redhat.com>
   */
  public static enum DependencyArtifact {
    // Non-errai
    GwtUser("gwt-user", "com.google.gwt"),
    GwtDev("gwt-dev", "com.google.gwt"),
    Guava("guava", "com.google.guava"),
    GuavaGwt("guava-gwt", "com.google.guava"),
    Hsq("hsqldb", "org.hsqldb"),
    JUnit("junit", "junit"),
    GwtSlf4j("gwt-slf4j", "de.benediktmeurer.gwt-slf4j"),
    JavaxInject("javax.inject", "javax.inject"),
    CdiApi("cdi-api", "javax.enterprise"),
    Jetty("jetty", "org.mortbay.jetty"),
    JettyPlus("jetty-plus", "org.mortbay.jetty"),
    JettyNaming("jetty-naming", "org.mortbay.jetty"),
    JsrApi("jsr250-api", "javax.annotation"),
    JavaxValidation("validation-api", "javax.validation"),
    JavaxValidationSources("validation-api", "javax.validation", "sources", null),
    HibernateAnnotations("hibernate-commons-annotations", "org.hibernate.common"),
    HibernateJpa("hibernate-jpa-2.0-api", "org.hibernate.javax.persistence"),
    HibernateCore("hibernate-core", "org.hibernate"),
    HibernateEntityManager("hibernate-entitymanager", "org.hibernate"),
    HibernateValidator("hibernate-validator", "org.hibernate"),
    HibernateValidatorSources("hibernate-validator", "org.hibernate", "sources", null),
    JbossLogging("jboss-logging", "org.jboss.logging"),
    JaxrsApi("jaxrs-api", "org.jboss.resteasy"),
    JbossInterceptors("jboss-interceptors-api_1.1_spec", "org.jboss.spec.javax.interceptor"),
    JbossTransaction("jboss-transaction-api_1.1_spec", "org.jboss.spec.javax.transaction"),
    WildflyDist("wildfly-dist", "org.wildfly"),
    WeldServletCore("weld-servlet-core", "org.jboss.weld.servlet"),
    WeldSeCore("weld-se-core", "org.jboss.weld.se"),
    WeldCore("weld-core", "org.jboss.weld"),
    WeldApi("weld-api", "org.jboss.weld"),
    WeldSpi("weld-spi", "org.jboss.weld"),
    XmlApis("xml-apis", "xml-apis"),
    RestEasyCdi("resteasy-cdi", "org.jboss.resteasy"),

    // tests
    GwtMockito("gwtmockito", "com.google.gwt.gwtmockito"),

    // plugins
    Clean("maven-clean-plugin", "org.apache.maven.plugins"),
    Dependency("maven-dependency-plugin", "org.apache.maven.plugins"),
    Compiler("maven-compiler-plugin", "org.apache.maven.plugins"),
    GwtPlugin("gwt-maven-plugin", "org.codehaus.mojo"),
    War("maven-war-plugin", "org.apache.maven.plugins"),
    WildflyPlugin("wildfly-maven-plugin", "org.wildfly.plugins"),
    Surefire("maven-surefire-plugin", "org.apache.maven.plugins"),
    EclipseMavenPlugin("lifecycle-mapping", "org.eclipse.m2e"),

    // errai
    ErraiVersionMaster("errai-version-master", "org.jboss.errai.bom"),
    ErraiBom("errai-bom", "org.jboss.errai.bom"),
    ErraiParent("errai-parent"),

    ErraiNetty("netty", "org.jboss.errai.io.netty"),
    ErraiJboss("errai-cdi-jboss"),
    JbossSupport("errai-jboss-as-support"),
    ErraiCommon("errai-common"),
    ErraiTools("errai-tools"),
    ErraiBus("errai-bus"),
    ErraiCdiClient("errai-cdi-client"),
    ErraiCdiClientTest("errai-cdi-client", "org.jboss.errai", null, "test-jar"),
    ErraiCdiServer("errai-cdi-server"),
    ErraiCdiJetty("errai-cdi-jetty"),
    ErraiCodegenGwt("errai-codegen-gwt"),
    ErraiIoc("errai-ioc"),
    ErraiDataBinding("errai-data-binding"),
    ErraiJavaxEnterprise("errai-javax-enterprise"),
    ErraiJaxrsClient("errai-jaxrs-client"),
    ErraiJaxrsProvider("errai-jaxrs-provider"),
    ErraiJpaClient("errai-jpa-client"),
    ErraiJpaDatasync("errai-jpa-datasync"),
    ErraiNavigation("errai-navigation"),
    ErraiUi("errai-ui"),
    ErraiCordova("errai-cordova"),
    ErraiSecurityClient("errai-security-client"),
    ErraiSecurityServer("errai-security-server"),
    ErraiSecurityPicketlink("errai-security-picketlink"),
    CordovaPlugin("cordova-maven-plugin"),
    ErraiHtml5("errai-html5");

    private final String artifactId;
    private final String groupId;
    private final String classifier;
    private final String type;

    private DependencyArtifact(final String artifactId, final String groupId, final String classifier, final String type) {
      this.artifactId = artifactId;
      this.groupId = groupId;
      this.classifier = classifier;
      this.type = type;
    }

    private DependencyArtifact(final String artifactId, final String groupId) {
      this(artifactId, groupId, null, null);
    }

    private DependencyArtifact(final String id) {
      this(id, ERRAI_GROUP_ID);
    }

    /**
     * @return The artifact id of this dependency.
     */
    public String getArtifactId() {
      return artifactId;
    }

    /**
     * @return The group id of this dependency.
     */
    public String getGroupId() {
      return groupId;
    }

    /**
     * Returns the string {@code groupId} + ":" + {@code artifactId}.
     */
    @Override
    public String toString() {
            return String.format("%s:%s", groupId, artifactId);
    }

    private static Map<String, DependencyArtifact> artifacts = new HashMap<String, ArtifactVault.DependencyArtifact>();

    static {
      for (final DependencyArtifact artifact : DependencyArtifact.values()) {
        artifacts.put(artifact.getGroupId() + ":" + artifact.getArtifactId(), artifact);
      }
    }

    /**
     * Lookup a {@link DependencyArtifact} by the unique combination of it's
     * group id and artifact id.
     */
    public static DependencyArtifact valueOf(String groupId, String artifactId) {
      return artifacts.get(groupId + ":" + artifactId);
    }

    public String getClassifier() {
      return classifier;
    }

    public String getType() {
      return type;
    }
  }

  /**
   * Blacklist of Maven dependencies which cannot be deployed in various
   * profiles.
   */
  private static final Map<String, Set<DependencyArtifact>> blacklist = new HashMap<String, Set<DependencyArtifact>>();

  static {
    // Wildfly/Jboss blacklist
    blacklist.put(AbstractBaseFacet.MAIN_PROFILE, new HashSet<DependencyArtifact>());
    final Set<DependencyArtifact> mainProfileBlacklist = blacklist.get(AbstractBaseFacet.MAIN_PROFILE);
    mainProfileBlacklist.add(DependencyArtifact.ErraiTools);
    mainProfileBlacklist.add(DependencyArtifact.ErraiSecurityClient);
    mainProfileBlacklist.add(DependencyArtifact.ErraiJboss);
    mainProfileBlacklist.add(DependencyArtifact.Hsq);
    mainProfileBlacklist.add(DependencyArtifact.JUnit);
    mainProfileBlacklist.add(DependencyArtifact.ErraiNetty);
    mainProfileBlacklist.add(DependencyArtifact.GwtSlf4j);
    mainProfileBlacklist.add(DependencyArtifact.ErraiCodegenGwt);
    mainProfileBlacklist.add(DependencyArtifact.JavaxInject);
    mainProfileBlacklist.add(DependencyArtifact.CdiApi);
    mainProfileBlacklist.add(DependencyArtifact.ErraiCdiJetty);
    mainProfileBlacklist.add(DependencyArtifact.GuavaGwt);
    mainProfileBlacklist.add(DependencyArtifact.JsrApi);
    mainProfileBlacklist.add(DependencyArtifact.JavaxValidation);
    mainProfileBlacklist.add(DependencyArtifact.HibernateAnnotations);
    mainProfileBlacklist.add(DependencyArtifact.HibernateJpa);
    mainProfileBlacklist.add(DependencyArtifact.HibernateCore);
    mainProfileBlacklist.add(DependencyArtifact.HibernateEntityManager);
    mainProfileBlacklist.add(DependencyArtifact.HibernateValidator);
    mainProfileBlacklist.add(DependencyArtifact.ErraiDataBinding);
    mainProfileBlacklist.add(DependencyArtifact.ErraiJavaxEnterprise);
    mainProfileBlacklist.add(DependencyArtifact.ErraiJaxrsClient);
    mainProfileBlacklist.add(DependencyArtifact.ErraiJpaClient);
    mainProfileBlacklist.add(DependencyArtifact.ErraiNavigation);
    mainProfileBlacklist.add(DependencyArtifact.ErraiUi);
    mainProfileBlacklist.add(DependencyArtifact.JbossLogging);
    mainProfileBlacklist.add(DependencyArtifact.JaxrsApi);
    mainProfileBlacklist.add(DependencyArtifact.JbossInterceptors);
    mainProfileBlacklist.add(DependencyArtifact.JbossTransaction);
    mainProfileBlacklist.add(DependencyArtifact.WeldServletCore);
    mainProfileBlacklist.add(DependencyArtifact.WeldCore);
    mainProfileBlacklist.add(DependencyArtifact.WeldApi);
    mainProfileBlacklist.add(DependencyArtifact.WeldSpi);
    mainProfileBlacklist.add(DependencyArtifact.XmlApis);
    mainProfileBlacklist.add(DependencyArtifact.JettyNaming);
    mainProfileBlacklist.add(DependencyArtifact.RestEasyCdi);
    mainProfileBlacklist.add(DependencyArtifact.ErraiCordova);
    mainProfileBlacklist.add(DependencyArtifact.ErraiHtml5);
    // Source Dependencies
    mainProfileBlacklist.add(DependencyArtifact.JavaxValidationSources);
    mainProfileBlacklist.add(DependencyArtifact.HibernateValidatorSources);
  }

  public static boolean isBlacklisted(final String identifier) {
    for (final String profile : blacklist.keySet()) {
      if (blacklist.get(profile).contains(identifier))
        return true;
    }

    return false;
  }

  public static boolean isBlacklisted(final Dependency dep) {
    return isBlacklisted(dep.getCoordinate().getGroupId() + ":" + dep.getCoordinate().getArtifactId());
  }

  public static String getBlacklistedProfile(final String identifier) {
    for (final String profile : blacklist.keySet()) {
      if (blacklist.get(profile).contains(identifier))
        return profile;
    }

    return null;
  }

  public static String getBlacklistedProfile(final Dependency dep) {
    return getBlacklistedProfile(dep.getCoordinate().getGroupId() + ":" + dep.getCoordinate().getArtifactId());
  }

  public static Collection<String> getBlacklistProfiles() {
    return blacklist.keySet();
  }

  public static Collection<DependencyArtifact> getBlacklistedArtifacts(final String profileId) {
    final Set<DependencyArtifact> artifacts = blacklist.get(profileId);

    return (artifacts != null ? artifacts : new ArrayList<DependencyArtifact>(0));
  }

}
