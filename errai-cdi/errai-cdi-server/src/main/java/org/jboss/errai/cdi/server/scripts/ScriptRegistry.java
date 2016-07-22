/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.jboss.errai.cdi.server.scripts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

/**
 * Script registry for scripts loaded at runtime (i.e. dynamic plugins).
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ApplicationScoped
public class ScriptRegistry {

  private final Map<String, Set<String>> scripts = new HashMap<String, Set<String>>();

  public void addScript(final String ns, final String url) {
    scripts.computeIfAbsent(ns, k -> new HashSet<String>()).add(url);
  }

  public void removeScript(final String ns, final String url) {
    scripts.getOrDefault(ns, new HashSet<String>()).remove(url);
  }

  public void removeScripts(final String key) {
    scripts.remove(key);
  }

  public Set<String> getAllScripts() {
    return scripts.values().stream().flatMap(s -> s.stream()).collect(Collectors.toSet());
  }

  public boolean isEmpty() {
    return scripts.isEmpty();
  }

  @Override
  public String toString() {
    return "ScriptRegistry [scripts=" + scripts + "]";
  }
}
