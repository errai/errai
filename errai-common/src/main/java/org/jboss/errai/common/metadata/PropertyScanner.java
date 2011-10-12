/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.common.metadata;

import com.google.common.base.Predicate;
import org.reflections.scanners.AbstractScanner;
import org.reflections.vfs.Vfs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Collects all property files and merges them under a single key
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Aug 10, 2010
 */
public class PropertyScanner extends AbstractScanner {
  Predicate<String> predicate;
  Map<String, Properties> properties = new HashMap<String, Properties>();

  public PropertyScanner(Predicate<String> predicate) {
    this.predicate = predicate;
  }

  public boolean acceptsInput(String file) {
    return predicate.apply(file);
  }

  public void scan(Vfs.File file) {

    String key = file.getName();
    if (null == properties.get(key)) {
      properties.put(key, new Properties());
    }

    try {
      properties.get(key).load(file.openInputStream());
    }
    catch (IOException e) {
      throw new RuntimeException("Failed to load properties: " + file.getFullPath(), e);
    }
  }

  public void scan(Object cls) {
    throw new UnsupportedOperationException(); //shouldn't get here
  }

  public Map<String, Properties> getProperties() {
    return properties;
  }
}
