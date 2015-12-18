/*
 * Copyright (C) 2009 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.common.metadata;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DeploymentContext and {@link PackagingUtil}
 * identify and unpack nested subdeployments (i.e. WAR inside EAR) before passing the resulting URL's to
 * the Reflections implementation.
 * <p/>
 * Calling {@link #close()} deletes the temporary created archive files.
 * Subsequent call to Reflection rely on classloading and don't need these artifacts anymore.
 *
 * @author Heiko Braun <hbraun@redhat.com>
 */
public class DeploymentContext {
  private List<URL> configUrls;
  private Map<String, File> subContexts = new HashMap<String, File>();
  private Set<String> processedUrls = new HashSet<String>();
  private Set<File> createdTmpFiles = new HashSet<File>();

  private Logger log = LoggerFactory.getLogger(DeploymentContext.class);

  public DeploymentContext(List<URL> configUrls) {
    this.configUrls = configUrls;
  }

  public List<URL> getConfigUrls() {
    return configUrls;
  }

  public Map<String, File> getSubContexts() {
    return subContexts;
  }

  public boolean hasProcessed(File file) {
    return processedUrls.contains(file.getAbsolutePath());
  }

  public void markProcessed(File file) {
    processedUrls.add(file.getAbsolutePath());
  }

  public List<URL> process() {
    PackagingUtil.process(this);

    final List<URL> superAndSubContexts = new ArrayList<URL>();

    for (Map.Entry<String, File> entry : subContexts.entrySet()) {
      File unzipped = entry.getValue();
      try {
        superAndSubContexts.add(unzipped.toURI().toURL());
      }
      catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }

    // orig urls needed? could be refactored...
    superAndSubContexts.addAll(configUrls);
    return superAndSubContexts;
  }

  public void markTmpFile(File file) {
    createdTmpFiles.add(file);
  }

  public void close() {
    for (File f : createdTmpFiles) {
      boolean deleted = deleteDirectory(f);
      if (!deleted) {
        //note: use an error message instead of an exception
        log.error("failed to cleanup: files were not deleted: " + f.getPath() + " (exists:" + f.exists() + ")");
      }
    }
  }

  static public boolean deleteDirectory(File path) {
    if (path.exists()) {
      File[] files = path.listFiles();
      for (int i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) {
          deleteDirectory(files[i]);
        }
        else {
          files[i].delete();
        }
      }
    }
    return (path.delete());
  }
}
