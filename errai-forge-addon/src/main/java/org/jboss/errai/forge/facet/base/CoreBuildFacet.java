package org.jboss.errai.forge.facet.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.config.ProjectConfig.ProjectProperty;
import org.jboss.errai.forge.constant.DefaultVault.DefaultValue;
import org.jboss.errai.forge.constant.PomPropertyVault.Property;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;

/**
 * This facet configures the source folders, build output directory, and pom
 * properties for a project.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ ProjectConfig.class })
public class CoreBuildFacet extends AbstractBaseFacet {

  public static final String DEV_CONTEXT = "${project.artifactId}";
  public static final String JBOSS_HOME = "${project.build.directory}/jboss-as-7.1.1.Final";

  private String getErraiVersion() {
    return getFaceted().getFacet(ProjectConfig.class).getProjectProperty(ProjectProperty.ERRAI_VERSION, String.class);
  }

  public static String getSourceDirectory(final Project project) {
    final MavenFacet coreFacet = project.getFacet(MavenFacet.class);

    return coreFacet.getModel().getBuild().getSourceDirectory();
  }

  public String getSourceDirectory() {
    return getSourceDirectory(getProject());
  }

  public String getResourceDirectory() {
    return getResourceDirectory(getProject());
  }

  public static String getResourceDirectory(final Project project) {
    final MavenFacet coreFacet = project.getFacet(MavenFacet.class);
    final Model model = coreFacet.getModel();
    if (model.getBuild() == null)
      model.setBuild(new Build());
    final List<Resource> resources = model.getBuild().getResources();

    /*
     * FIXME need to decide on a better way to select a resource directory. For
     * now, sort by directory paths and return the first path that is not the
     * source folder (so that the result is at least consistent).
     */

    final List<String> directories = new ArrayList<String>(resources.size());
    for (final Resource res : resources) {
      if (!res.getDirectory().equals(getSourceDirectory(project))) {
        directories.add(res.getDirectory());
      }
    }

    if (directories.isEmpty())
      return null;

    Collections.sort(directories);

    return directories.get(0);
  }

  @Override
  public boolean install() {
    final MavenFacet coreFacet = getProject().getFacet(MavenFacet.class);
    final Model pom = coreFacet.getModel();
    Build build = pom.getBuild();
    if (build == null) {
      build = new Build();
      pom.setBuild(build);
    }

    pom.addProperty(Property.JbossHome.getName(), JBOSS_HOME);
    pom.addProperty(Property.DevContext.getName(), DEV_CONTEXT);
    pom.addProperty(Property.ErraiVersion.getName(), getErraiVersion());

    if (build.getSourceDirectory() == null)
      build.setSourceDirectory(DefaultValue.SourceDirectory.getDefaultValue());

    Resource res = getResource(build.getSourceDirectory(), build.getResources());
    if (res == null) {
      res = new Resource();
      res.setDirectory(build.getSourceDirectory());
      build.addResource(res);
    }

    if (build.getResources().size() < 2) {
      res = getResource(DefaultValue.ResourceDirectory.getDefaultValue(), build.getResources());
      if (res == null) {
        res = new Resource();
        res.setDirectory(DefaultValue.ResourceDirectory.getDefaultValue());
        build.addResource(res);
      }
    }

    coreFacet.setModel(pom);

    return true;
  }

  @Override
  public boolean isInstalled() {
    final MavenFacet coreFacet = getProject().getFacet(MavenFacet.class);
    final Model pom = coreFacet.getModel();
    final Build build = pom.getBuild();

    Properties properties = pom.getProperties();
    return !(build == null
            || !properties.containsKey(Property.JbossHome.getName())
            || !properties.get(Property.JbossHome.getName()).equals(JBOSS_HOME)
            || !properties.containsKey(Property.DevContext.getName())
            || !properties.get(Property.DevContext.getName()).equals(DEV_CONTEXT)
            || !properties.containsKey(Property.ErraiVersion.getName())
            || !properties.get(Property.ErraiVersion.getName()).equals(getErraiVersion())
            || getResource(build.getSourceDirectory(), build.getResources()) == null
            || build.getResources().size() < 2);
  }

  private Resource getResource(String relPath, List<Resource> resources) {
    for (final Resource res : resources) {
      if (res.getDirectory().equals(relPath))
        return res;
    }

    return null;
  }
}
