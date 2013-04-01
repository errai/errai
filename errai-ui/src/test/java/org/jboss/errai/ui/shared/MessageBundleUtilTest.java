/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.ui.shared;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author eric.wittmann@redhat.com
 */
public class MessageBundleUtilTest {

  /**
   * Test method for {@link org.jboss.errai.ui.shared.MessageBundleUtil#getLocaleFromBundleFilename(java.lang.String)}.
   */
  @Test
  public void testGetLocaleFromBundleFilename() {
    Assert.assertEquals(null, MessageBundleUtil.getLocaleFromBundleFilename("myBundle.json").getLang());
    Assert.assertEquals(null, MessageBundleUtil.getLocaleFromBundleFilename("myBundle.json").getRegion());
    Assert.assertEquals(null, MessageBundleUtil.getLocaleFromBundleFilename("myBundle_en_US.other"));

    Assert.assertEquals("en", MessageBundleUtil.getLocaleFromBundleFilename("myBundle_en_US.json").getLang());
    Assert.assertEquals("US", MessageBundleUtil.getLocaleFromBundleFilename("myBundle_en_US.json").getRegion());
    Assert.assertEquals("en", MessageBundleUtil.getLocaleFromBundleFilename("myBundle_en_GB.json").getLang());
    Assert.assertEquals("GB", MessageBundleUtil.getLocaleFromBundleFilename("myBundle_en_GB.json").getRegion());
    Assert.assertEquals("en", MessageBundleUtil.getLocaleFromBundleFilename("myBundle_en.json").getLang());
    Assert.assertEquals(null, MessageBundleUtil.getLocaleFromBundleFilename("myBundle_en.json").getRegion());

    Assert.assertEquals("fr", MessageBundleUtil.getLocaleFromBundleFilename("Some-Other-Bundle_fr_CA.json").getLang());
    Assert.assertEquals("CA", MessageBundleUtil.getLocaleFromBundleFilename("Some-Other-Bundle_fr_CA.json").getRegion());
    Assert.assertEquals("fr", MessageBundleUtil.getLocaleFromBundleFilename("Some-Other-Bundle_fr_FR.json").getLang());
    Assert.assertEquals("FR", MessageBundleUtil.getLocaleFromBundleFilename("Some-Other-Bundle_fr_FR.json").getRegion());
    Assert.assertEquals("fr", MessageBundleUtil.getLocaleFromBundleFilename("Some-Other-Bundle_fr.json").getLang());
    Assert.assertEquals(null, MessageBundleUtil.getLocaleFromBundleFilename("Some-Other-Bundle_fr.json").getRegion());
  }

}
