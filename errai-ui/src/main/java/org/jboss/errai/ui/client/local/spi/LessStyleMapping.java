/**
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

package org.jboss.errai.ui.client.local.spi;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of this class get created by the {@code LessStyleGenerator} and will create a mapping
 * from the obfuscated selector names to the original ones.
 *
 * @see org.jboss.errai.ui.rebind.less.LessStyleGenerator
 * @author edewit@redhat.com
 */
public abstract class LessStyleMapping {

  protected Map<String, String> styleNameMapping = new HashMap<String, String>();

  public String get(String styleName) {
    return styleNameMapping.get(styleName);
  }
}
