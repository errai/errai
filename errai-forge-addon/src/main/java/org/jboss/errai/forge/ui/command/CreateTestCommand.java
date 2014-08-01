package org.jboss.errai.forge.ui.command;

import static org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact.*;

import java.io.File;

import javax.inject.Inject;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.errai.forge.util.VersionFacet;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.maven.projects.facets.MavenDependencyFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

public abstract class CreateTestCommand extends AbstractProjectCommand {

  @Inject
  protected ProjectFactory projectFactory;

  @Override
  public UICommandMetadata getMetadata(UIContext context) {
    return Metadata.forCommand(getClass())
            .name("Errai: " + getCommandName())
            .category(Categories.create("Project", "Errai"))
            .description(getCommandDescription());
  }
  
  protected abstract String getCommandName();
  protected abstract String getCommandDescription();

  protected String relativeFilePathFromClassName(final String testClazzName) {
    return testClazzName.replace('.', File.separatorChar) + ".java";
  }

  protected File getTestSourceDirectory(final Project project) {
    final MavenFacet mavenFacet = project.getFacet(MavenFacet.class);
    final Model pom = mavenFacet.getModel();
    Build build = pom.getBuild();
    
    if (build == null) {
      build = new Build();
      pom.setBuild(build);
    }
  
    String testSourceDirectoryPath = build.getTestSourceDirectory();
    
    if (testSourceDirectoryPath == null) {
      testSourceDirectoryPath = "src/test/java";
      build.setSourceDirectory(testSourceDirectoryPath);
    }
  
    return new File(project.getRootDirectory().getUnderlyingResourceObject(),
            testSourceDirectoryPath).getAbsoluteFile();
  }

  protected String getSimpleName(final String className) {
    final String simpleClassName = className.replaceAll("^.*\\.", "");
    return simpleClassName;
  }

  protected String getPackage(final String className) {
    final String classPackageName = className.replaceAll("\\.[^.]*$", "");
    return classPackageName;
  }

  @Override
  protected boolean isProjectRequired() {
    return true;
  }

  @Override
  protected ProjectFactory getProjectFactory() {
    return projectFactory;
  }

  protected void addTestScopedDependency(final Project project, final DependencyArtifact artifact) {
    final DependencyBuilder depBuilder = DependencyBuilder.create(artifact.toString());
    final MavenDependencyFacet dependencyFacet = project.getFacet(MavenDependencyFacet.class);
    final VersionFacet versionFacet = project.getFacet(VersionFacet.class);

    if (!dependencyFacet.hasDirectDependency(depBuilder)) {
      if (!versionFacet.isManaged(depBuilder)) {
        depBuilder.setVersion(versionFacet.resolveVersion(GwtMockito));
      }
      dependencyFacet.addDirectDependency(depBuilder);
    }
  }

}
