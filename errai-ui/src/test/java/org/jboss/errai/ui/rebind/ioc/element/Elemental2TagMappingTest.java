/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ui.rebind.ioc.element;

import elemental2.dom.HTMLDivElement;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.junit.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.jboss.errai.ui.rebind.ioc.element.Elemental2TagMapping.getTags;
import static org.junit.Assert.assertEquals;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class Elemental2TagMappingTest {

  @Test
  public void testGetTags() {
    assertEquals("null should return no tag", emptyList(), getTags(null));

    assertEquals("Object.class should not have any mapped tag name", emptyList(), getTags(
            MetaClassFactory.get(Object.class)));

    assertEquals("String.class should not have any mapped tag name", emptyList(), getTags(MetaClassFactory.get(String.class)));

    assertEquals("HTMLDivElement should have a tag mapped to it", singletonList("div"), getTags(MetaClassFactory.get(HTMLDivElement.class)));

    assertEquals("HTMLDivElement subclass should have a tag mapped to it", singletonList("div"),
            getTags(MetaClassFactory.get(CustomElement.class)));

    assertEquals("HTMLDivElement subclass should have a tag mapped to it", singletonList("div"),
            getTags(MetaClassFactory.get(CustomElement.Child.class)));
  }

}