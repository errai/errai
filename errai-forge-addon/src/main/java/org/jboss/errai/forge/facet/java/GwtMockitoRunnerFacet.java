/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.forge.facet.java;

import org.jboss.errai.forge.facet.resource.AbstractFileResourceFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FacetConstraint({ JavaSourceFacet.class })
public class GwtMockitoRunnerFacet extends AbstractFileResourceFacet {

  private static final String GWT_MOCKITO_RUNNER_REL_PATH = "org/jboss/errai/GwtMockitoRunnerExtension.java";
  
  public GwtMockitoRunnerFacet() {
  }

  @Override
  public String getRelFilePath() {
    final JavaSourceFacet sourceFacet = getProject().getFacet(JavaSourceFacet.class);
    final File testSourceDirectory = sourceFacet.getTestSourceDirectory().getUnderlyingResourceObject().getAbsoluteFile();
    final File projectRoot = getProject().getRootDirectory().getUnderlyingResourceObject().getAbsoluteFile();
    
    final File absoluteResult = new File(testSourceDirectory, GWT_MOCKITO_RUNNER_REL_PATH)
            .getAbsoluteFile();

    return absoluteResult.getPath().replace(projectRoot.getPath(), "");
  }

  @Override
  protected String getResourceContent() throws Exception {
    return readResource("/org/jboss/errai/forge/facet/java/GwtMockitoRunnerExtension.java").toString();
  }
  
  public boolean addBlacklistedPackage(final String packageName) {
    final JavaSourceFacet sourceFacet = getProject().getFacet(JavaSourceFacet.class);
    final JavaResource gwtRunner = getGwtRunnerResource(sourceFacet);
    final String gwtRunnerContents = gwtRunner.getContents();
    
    final Matcher packageAddMatcher = generatePatternForPackageAddStatement(packageName).matcher(gwtRunnerContents);
    if (packageAddMatcher.find()) {
      return false;
    }
    
    final Pattern pattern = Pattern.compile("\\s*\n([^\\S\n]*)return\\s+blacklisted\\s*;");
    final Matcher matcher = pattern.matcher(gwtRunnerContents);
    
    if (!matcher.find()) {
      throw new IllegalStateException(String.format("%s has been modified.", GWT_MOCKITO_RUNNER_REL_PATH));
    }
    
    final String updatedGwtRunnerContents = new StringBuilder(gwtRunnerContents).insert(matcher.start(),
            "\n" + matcher.group(1) + "blacklisted.add(\"" + packageName + "\");").toString();
    gwtRunner.setContents(updatedGwtRunnerContents);
    
    return true;
  }

  private JavaResource getGwtRunnerResource(final JavaSourceFacet sourceFacet) {
      return sourceFacet.getTestJavaResource(GWT_MOCKITO_RUNNER_REL_PATH);
  }
  
  public boolean removeBlacklistedPackage(final String packageName) {
    final JavaSourceFacet sourceFacet = getProject().getFacet(JavaSourceFacet.class);
    final JavaResource gwtRunner = getGwtRunnerResource(sourceFacet);
    final String gwtRunnerContents = gwtRunner.getContents();
    
    final Matcher matcher = generatePatternForPackageAddStatement(packageName).matcher(
            gwtRunnerContents);

    if (matcher.find()) {
      final String updatedGwtRunnerContents = new StringBuilder(gwtRunnerContents).replace(matcher.start(),
              matcher.end(), "").toString();
      gwtRunner.setContents(updatedGwtRunnerContents);

      return true;
    }
    else {
      return false;
    }
  }
  
  public Set<String> getBlacklistedPackages() {
    final JavaSourceFacet sourceFacet = getProject().getFacet(JavaSourceFacet.class);
    final JavaResource gwtRunner = getGwtRunnerResource(sourceFacet);
    final String gwtRunnerContents = gwtRunner.getContents();

    final Pattern pattern = Pattern.compile("blacklisted\\s*\\.\\s*add\\s*\\(\\s*(\"|')(.*?)\\1\\s*\\);");
    final Matcher matcher = pattern.matcher(gwtRunnerContents);
    
    final Set<String> packages = new HashSet<String>();
    
    while (matcher.find()) {
      packages.add(matcher.group(2));
    }

    return packages;
  }

  private Pattern generatePatternForPackageAddStatement(final String packageName) {
    return Pattern.compile(
            "\\s*blacklisted\\s*\\.\\s*add\\s*\\(\\s*(\"|')" + Pattern.quote(packageName) + "\\1\\s*\\);");
  }
}
