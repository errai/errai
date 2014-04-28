package org.jboss.errai.forge.facet.plugin;

import static org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact.*;
import static org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder.*;

import java.util.Arrays;
import java.util.Collections;

import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.projects.MavenFacet;

@FacetConstraint({ MavenFacet.class })
public class SurefirePluginFacet extends AbstractProfilePluginFacet {
  
  public SurefirePluginFacet() {
    profileId = "integration-test";
    pluginArtifact = Surefire;
    dependencies = Collections.emptyList();
    executions = Collections.emptyList();
    
    configurations = Arrays.<ConfigurationElement>asList(
            create().setName("skipTests").setText("false"),
            create().setName("forkMode").setText("always"),
            create().setName("argLine").setText("-Xmx1500m"),
            create().setName("additionalClasspathElements")
              .addChild(create().setName("additionalClasspathElement").setText("${basedir}/target/classes"))
              .addChild(create().setName("additionalClasspathElement").setText("${basedir}/target/test-classes"))
              .addChild(create().setName("additionalClasspathElement").setText("${basedir}/src/main/java"))
              .addChild(create().setName("additionalClasspathElement").setText("${basedir}/src/test/java")),
            create().setName("useSystemClassLoader").setText("false"),
            create().setName("useManifestOnlyJar").setText("true"),
            create().setName("systemProperties")
              .addChild(create().setName("property")
                      .addChild(create().setName("name").setText("java.io.tmpdir"))
                      .addChild(create().setName("value").setText("${project.build.directory}"))
               )
              .addChild(create().setName("property")
                      .addChild(create().setName("name").setText("log4j.output.dir"))
                      .addChild(create().setName("value").setText("${project.build.directory}"))
               )
              .addChild(create().setName("property")
                      .addChild(create().setName("name").setText("errai.marshalling.server.classOutput.enabled"))
                      .addChild(create().setName("value").setText("false"))
               )
              .addChild(create().setName("property")
                      .addChild(create().setName("name").setText("org.jboss.errai.bus.do_long_poll"))
                      .addChild(create().setName("value").setText("false"))
               )
              .addChild(create().setName("property")
                      .addChild(create().setName("name").setText("errai.devel.nocache"))
                      .addChild(create().setName("value").setText("true"))
               ),
            create().setName("includes")
              .addChild(create().setName("include").setText("**/*Test.java"))
              .addChild(create().setName("include").setText("**/*Tests.java"))
            );
  }

}
