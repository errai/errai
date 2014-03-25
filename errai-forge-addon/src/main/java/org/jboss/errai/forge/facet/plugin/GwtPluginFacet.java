package org.jboss.errai.forge.facet.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.errai.forge.constant.PomPropertyVault.Property;
import org.jboss.errai.forge.facet.base.CoreBuildFacet;
import org.jboss.errai.forge.facet.resource.GwtHostPageFacet;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.addon.maven.plugins.Execution;
import org.jboss.forge.addon.maven.plugins.ExecutionBuilder;

/**
 * This facet configures the gwt-maven-plugin in the build section of the pom
 * file.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ CoreBuildFacet.class, GwtHostPageFacet.class })
public class GwtPluginFacet extends AbstractPluginFacet {

  public GwtPluginFacet() {
    pluginArtifact = DependencyArtifact.GwtPlugin;
    dependencies = new ArrayList<DependencyBuilder>(0);
    executions = Arrays.asList(new Execution[] {
        // Note: phase and id must be explicitly set, otherwise forge will write
        // "null" as values to the tags
        ExecutionBuilder.create().setId("resources").setPhase("process-resources").addGoal("resources"),
        ExecutionBuilder.create().setId("compile").setPhase("prepare-package").addGoal("compile")
    });
    configurations = Arrays
            .asList(new ConfigurationElement[] {
                ConfigurationElementBuilder.create().setName("logLevel").setText("INFO"),
                ConfigurationElementBuilder.create().setName("noServer").setText("false"),
                ConfigurationElementBuilder.create().setName("server")
                        .setText("org.jboss.errai.cdi.server.gwt.JBossLauncher"),
                ConfigurationElementBuilder.create().setName("disableCastChecking").setText("true"),
                ConfigurationElementBuilder.create().setName("runTarget").setText("${errai.dev.context}/index.html"),
                ConfigurationElementBuilder.create().setName("soyc").setText("false"),
                ConfigurationElementBuilder.create().setName("hostedWebapp"),
                ConfigurationElementBuilder
                        .create()
                        .setName("extraJvmArgs")
                        .setText(
                                "-Xmx712m "
                                        + "-XX:CompileThreshold=7000 "
                                        + "-XX:MaxPermSize=128M "
                                        + "-D"
                                        + Property.JbossHome.getName()
                                        + "="
                                        + Property.JbossHome.invoke()
                                        + " "
                                        + "-D"
                                        + Property.DevContext.getName()
                                        + "="
                                        + Property.DevContext.invoke()
                                        + " "
                                        + "-Derrai.jboss.javaagent.path=${settings.localRepository}/org/jboss/errai/errai-client-local-class-hider/"
                                        + Property.ErraiVersion.invoke() + "/errai-client-local-class-hider-"
                                        + Property.ErraiVersion.invoke() + ".jar"
                        )
            });
  }

  private void init() {
    for (final ConfigurationElement elem : configurations) {
      if (elem.getName().equals("hostedWebapp")) {
        ConfigurationElementBuilder.class.cast(elem).setText(WarPluginFacet.getWarSourceDirectory(getProject()));
        break;
      }
    }
  }
  
  @Override
  public Collection<ConfigurationElement> getConfigurations() {
    init();
    return super.getConfigurations();
  }

}
