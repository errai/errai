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

package org.jboss.errai.offline.linker;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.core.ext.Linker;
import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.AbstractLinker;
import com.google.gwt.core.ext.linker.Artifact;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.linker.Shardable;
import com.google.gwt.core.ext.linker.Transferable;
import com.google.gwt.core.ext.linker.impl.SelectionInformation;
import com.google.gwt.dev.util.collect.HashSet;

/**
 * Linker that generates permutation specific HTML5 Cache Manifest files for
 * Errai applications.
 * 
 * This linker is based on <a
 * href="https://code.google.com/p/google-web-toolkit/source/browse/
 * trunk/dev/core/src/
 * com/google/gwt/core/linker/SimpleAppCacheLinker.java?r=10136
 * ">SimpleAppCacheLinker</a>.
 * 
 * <p>
 * To use:
 * <ol>
 * <li>Define the linker in your gwt.xml module descriptor:
 * 
 * <pre>
 *   {@code <define-linker name="offline" class="org.jboss.errai.offline.linker.DefaultCacheManifestLinker" />}
 *   {@code <add-linker name="offline" />}
 * </pre>
 * 
 * </li>
 * 
 * <li>Add {@code manifest="YOURMODULENAME/errai.appcache"} to the
 * {@code <html>} tag in your host page e.g.,
 * {@code <html manifest="mymodule/errai.appcache">} <br>
 * <br>
 * </li>
 * 
 * <li>Add a mime-mapping to your web.xml file (you can skip this step if you
 * deploy the errai-javaee-all.jar as part of your application):
 * 
 * <pre>
 * {@code <mime-mapping>
 *   <extension>manifest</extension>
 *   <mime-type>text/cache-manifest</mime-type>
 * </mime-mapping>
 * }
 * </pre>
 * 
 * </li>
 * 
 * <li>Make sure the errai-common.jar file is deployed as part of your
 * application. It contains a servlet that will provide the correct user-agent
 * specific manifest file in response to requests to
 * YOURMODULENAME/errai.appcache</li>
 * </ol>
 * 
 * <p>
 * To obtain manifests that contain other files in addition to those generated
 * by this linker, create a class that inherits from this one and overrides
 * {@code otherCachedFiles()}, and use it as a linker instead:
 * 
 * <pre>
 * {@code @Shardable}
 * {@code @LinkerOrder(Order.POST)}
 * public class MyCacheManifestLinker extends DefaultCacheManifestLinker {
 *   {@code @Override}
 *   protected String[] otherCachedFiles() {
 *     return new String[] {"/my-app/index.html","/my-app/css/application.css"};
 *   }
 * }
 * </pre>
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Shardable
@LinkerOrder(Order.POST)
public class DefaultCacheManifestLinker extends AbstractLinker {
  private static final String MANIFEST = "appcache.manifest";

  @Transferable
  private class PermutationCacheManifestArtifact extends Artifact<PermutationCacheManifestArtifact> {
    private static final long serialVersionUID = 1L;

    private Set<String> cachedFiles = new HashSet<String>();
    private String name;
    private Map<String, String> props;

    public PermutationCacheManifestArtifact(Class<? extends Linker> linker, String name, Map<String, String> props) {
      super(linker);
      this.name = name;
      this.props = props;
    }

    @Override
    public int hashCode() {
      return cachedFiles.hashCode();
    }

    @Override
    protected int compareToComparableArtifact(PermutationCacheManifestArtifact o) {
      return name.compareTo(o.name);
    }

    @Override
    protected Class<PermutationCacheManifestArtifact> getComparableArtifactType() {
      return PermutationCacheManifestArtifact.class;
    }

    public void addCachedFile(String file) {
      cachedFiles.add(file);
    }

  }

  @Override
  public String getDescription() {
    return "to generate cache manifest files";
  }

  @Override
  public ArtifactSet link(TreeLogger logger, LinkerContext context, ArtifactSet artifacts, boolean onePermutation)
          throws UnableToCompleteException {

    ArtifactSet toReturn = new ArtifactSet(artifacts);
    if (toReturn.find(SelectionInformation.class).isEmpty()) {
      logger.log(TreeLogger.INFO, "devmode: generating empty " + MANIFEST);
      toReturn.add(emitString(logger, "# Empty in DevMode\n", "dev." + MANIFEST));
    }
    else if (onePermutation) {
      // Create an artifact representing the cache manifest for the current
      // permutation
      toReturn.add(createPermutationCacheManifestArtifact(context, logger, artifacts));
    }
    else {
      // Group permutations per user agent
      final Multimap<String, PermutationCacheManifestArtifact> permutations = ArrayListMultimap.create();
      for (PermutationCacheManifestArtifact pcma : artifacts.find(PermutationCacheManifestArtifact.class)) {
        permutations.put(pcma.props.get("user.agent"), pcma);
      }

      for (String userAgent : permutations.keySet()) {
        // Create a cache manifest file for every user agent
        toReturn.add(emitUserAgentCacheManifestFile(userAgent, permutations.get(userAgent), artifacts, logger));

      }

      logger.log(TreeLogger.INFO,
              "Make sure you have the following attribute added to your host page's <html> tag: <html manifest=\""
                      + context.getModuleFunctionName() + "/" + MANIFEST + "\">");
    }
    return toReturn;
  }

  /**
   * Override this method to include additional files in the manifest.
   */
  protected String[] otherCachedFiles() {
    return null;
  }

  /**
   * Creates the cache manifest artifact specific to the current permutation.
   * 
   * @param context
   *          the linker environment
   * @param logger
   *          the tree logger to record to
   * @param artifacts
   *          {@code null} in case no permutation specific artifacts exist
   */
  private Artifact<?> createPermutationCacheManifestArtifact(LinkerContext context, TreeLogger logger,
          ArtifactSet artifacts) {

    SelectionInformation si = artifacts.find(SelectionInformation.class).first();
    PermutationCacheManifestArtifact cacheArtifact = new PermutationCacheManifestArtifact(this.getClass(),
            si.getStrongName(), si.getPropMap());

    if (artifacts != null) {
      for (Artifact<?> artifact : artifacts) {
        if (artifact instanceof EmittedArtifact) {
          EmittedArtifact ea = (EmittedArtifact) artifact;
          String pathName = ea.getPartialPath();
          if (shouldBeCached(pathName)) {
            cacheArtifact.addCachedFile(pathName);
          }
        }
      }
    }
    return cacheArtifact;
  }

  /**
   * Emits the cache manifest file for the user agent represented by the
   * provided artifacts.
   * 
   * @param userAgent
   *          the user agent name
   * @param artifacts
   *          the user agent specific cache manifest artifacts
   * @param globalArtifacts
   *          all artifacts known to this linker
   * @param logger
   *          the tree logger to record to
   * @return a synthetic artifact representing the cache manifest file for the
   *         user agent
   * 
   * @throws UnableToCompleteException
   */
  private Artifact<?> emitUserAgentCacheManifestFile(String userAgent,
          Collection<PermutationCacheManifestArtifact> artifacts, ArtifactSet globalArtifacts, TreeLogger logger)
          throws UnableToCompleteException {

    // Add static external resources
    StringBuilder staticResoucesSb = new StringBuilder();
    String[] cacheExtraFiles = getCacheExtraFiles();
    for (int i = 0; i < cacheExtraFiles.length; i++) {
      staticResoucesSb.append(cacheExtraFiles[i]);
      staticResoucesSb.append("\n");
    }

    // Add generated resources
    Set<String> cacheableGeneratedResources = new HashSet<String>();

    // Add permutation independent resources
    if (globalArtifacts != null) {
      for (Artifact<?> a : globalArtifacts) {
        if (a instanceof EmittedArtifact) {
          EmittedArtifact ea = (EmittedArtifact) a;
          String pathName = ea.getPartialPath();
          if (shouldBeCached(pathName) && !isPermutationSpecific(globalArtifacts, pathName)) {
            cacheableGeneratedResources.add(pathName);
          }
        }
      }
    }

    // Add permutation specific resources
    if (artifacts != null) {
      for (PermutationCacheManifestArtifact artifact : artifacts) {
        for (String cachedFile : artifact.cachedFiles) {
          cacheableGeneratedResources.add(cachedFile);
        }
      }
    }

    // Build manifest
    StringBuilder sb = new StringBuilder();
    sb.append("CACHE MANIFEST\n");
    // we have to generate this unique id because the resources can change but
    // the hashed cache.html files can remain the same.
    sb.append("# Unique id #" + (new Date()).getTime() + "." + Math.random() + "\n");
    sb.append("\n");
    sb.append("CACHE:\n");
    sb.append("# Static app files\n");
    sb.append(staticResoucesSb.toString());
    sb.append("\n# Generated permutation specific app files");
    for (String resource : cacheableGeneratedResources) {
      sb.append("\n");
      sb.append(resource);
    }
    sb.append("\n\n");
    sb.append("# All other resources require the user to be online.\n");
    sb.append("NETWORK:\n");
    sb.append("*\n");

    // Create the user agent specific manifest as a new artifact and return it
    return emitString(logger, sb.toString(), userAgent + "." + MANIFEST);
  }

  /**
   * Obtains the extra files to include in the manifest. Ensures the returned
   * array is not null.
   */
  private String[] getCacheExtraFiles() {
    String[] cacheExtraFiles = otherCachedFiles();
    return cacheExtraFiles == null ? new String[0] : Arrays.copyOf(cacheExtraFiles, cacheExtraFiles.length);
  }

  private Set<String> permutationFiles;

  /**
   * Checks whether the provided file is specific to a permutation.
   * 
   * @param artifacts
   *          all artifacts passed to this linker
   * @param file
   *          the file to check
   * @return true if the file is specific to a permutation, otherwise false.
   * 
   */
  private boolean isPermutationSpecific(ArtifactSet artifacts, String file) {
    if (permutationFiles == null) {
      permutationFiles = new HashSet<String>();
      for (PermutationCacheManifestArtifact pcma : artifacts.find(PermutationCacheManifestArtifact.class)) {
        permutationFiles.addAll(pcma.cachedFiles);
      }
    }

    return permutationFiles.contains(file);
  }

  /**
   * Checks whether or not the provided file should be cached.
   * 
   * @param file
   *          the file to check
   * @return true if the file is cacheable and should therefore be listed in the
   *         cache manifest file, otherwise false.
   */
  private boolean shouldBeCached(String file) {
    return !(file.endsWith("symbolMap") || file.endsWith(".xml.gz") || file.endsWith("rpc.log")
            || file.endsWith("gwt.rpc") || file.endsWith("manifest.txt") || file.startsWith("rpcPolicyManifest") 
            || file.startsWith("deferredjs") || file.startsWith("hosted") || file.startsWith("junit") );
  }
}
