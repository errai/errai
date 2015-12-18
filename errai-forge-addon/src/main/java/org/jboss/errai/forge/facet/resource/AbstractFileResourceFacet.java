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

package org.jboss.errai.forge.facet.resource;

import org.jboss.errai.forge.facet.base.AbstractBaseFacet;

import java.io.*;

/**
 * Base class for facets that add required a resource file (such as a
 * .properties file). It is not the responsibility of this class to check the
 * content of the resource file. It simply checks for existence, and if
 * necessary installs a simple version of the required resource.
 * 
 * Concrete subclasses must assign the field
 * {@link AbstractFileResourceFacet#relFilePath relFilePath} and implement the
 * method {@link AbstractFileResourceFacet#getResourceContent()
 * getResourceContent}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class AbstractFileResourceFacet extends AbstractBaseFacet {

  /**
   * The path (relative to the project root directory) of the file resource to
   * be installed.
   */
  public abstract String getRelFilePath();

  @Override
  public boolean install() {
    final File file = getAbsoluteFilePath();
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      try {
        file.createNewFile();
      }
      catch (IOException e) {
        error(getClass().getSimpleName() + ": Could not make the file " + file.getAbsolutePath(), e);
        return false;
      }
    }
    FileWriter writer = null;
    try {
      writer = new FileWriter(file);
      writer.write(getResourceContent());
    }
    catch (IOException e) {
      error("Could not write to " + file.getAbsolutePath(), e);
      return false;
    }
    catch (Exception e) {
      error("Unexpected error while trying to add resource " + getRelFilePath(), e);
      return false;
    }
    finally {
      try {
        if (writer != null)
          writer.close();
      }
      catch (IOException e) {
        error("Could not close FileWriter for " + file.getAbsolutePath(), e);
      }
    }

    return true;
  }

  /**
   * Get a String to be written to the resource file being installed by this
   * class. This method is called if the resource file specified by this facet
   * does not exist and one must be created.
   * 
   * @return The text to be written to the resource file at
   *         {@link AbstractFileResourceFacet#relFilePath relFilePath}.
   */
  protected abstract String getResourceContent() throws Exception;

  /**
   * Reads a classpath resource into a {@link StringBuilder}.
   * 
   * @param resource
   *          The name of the classpath resource to be copied.
   * @return A {@link StringBuilder} containing the contents of the specified
   *         resource.
   */
  protected StringBuilder readResource(final String resource) throws IOException {
    final StringBuilder builder = new StringBuilder();
    final InputStream stream = getClass().getResourceAsStream(resource);
    final InputStreamReader reader = new InputStreamReader(stream);
    final char[] buf = new char[256];
    int read;
    while (true) {
      read = reader.read(buf);
      if (read == -1)
        break;
      builder.append(buf, 0, read);
    }
    reader.close();
    stream.close();

    return builder;
  }

  /**
   * Replace all occurrences of a {@link String} in a give {@link StringBuilder}
   * .
   * 
   * @param subject
   *          The {@link StringBuilder} to be modified.
   * @param toReplace
   *          The {@link String} to be replaced.
   * @param replacement
   *          The replacement {@link String}.
   */
  protected void replace(final StringBuilder subject, final String toReplace, final String replacement) {
    for (int fillerIndex = subject.indexOf(toReplace); fillerIndex > -1; fillerIndex = subject.indexOf(toReplace,
            fillerIndex)) {
      subject.replace(fillerIndex, fillerIndex + toReplace.length(), replacement);
    }
  }

  @Override
  public boolean isInstalled() {
    /*
     * Just check that the resource exists. Classes with more particular
     * requirements can override this.
     */
    return getAbsoluteFilePath().exists();
  }

  public File getAbsoluteFilePath() {
    return new File(getProject().getRootDirectory().getUnderlyingResourceObject(), getRelFilePath());
  }
  
  @Override
  public boolean uninstall() {
    // Do not actually delete file
    return true;
  }

}
