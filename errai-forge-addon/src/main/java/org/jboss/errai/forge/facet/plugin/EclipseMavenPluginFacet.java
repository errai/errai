package org.jboss.errai.forge.facet.plugin;

import org.apache.maven.model.PluginExecution;
import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.errai.forge.facet.base.CoreBuildFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Divya Dadlani <ddadlani@redhat.com>
 */
@FacetConstraint(CoreBuildFacet.class)
public class EclipseMavenPluginFacet extends AbstractPluginFacet {

    public Collection<PluginExecution> pluginExecutions;

    public EclipseMavenPluginFacet() {
        pluginArtifact = DependencyArtifact.EclipseMavenPlugin;
        configurations = new ArrayList<ConfigurationElement>(0);
        pluginExecutions = Arrays.asList(new
                PluginExecution[]{});

    }
}
