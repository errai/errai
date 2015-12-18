/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ui.shared;

/**
 * Contains locale info associated with a single message bundle file.
 *
 * @author eric.wittmann@redhat.com
 */
public class MessageBundleLocaleInfo {

  private String lang;
  private String region;

  /**
   * Constructor.
   */
  public MessageBundleLocaleInfo() {
  }

  /**
   * Constructor.
   * @param lang
   * @param region
   */
  public MessageBundleLocaleInfo(String lang, String region) {
    setLang(lang);
    setRegion(region);
  }

  /**
   * @return the lang
   */
  public String getLang() {
    return lang;
  }

  /**
   * @param lang the lang to set
   */
  public void setLang(String lang) {
    this.lang = lang;
  }

  /**
   * @return the region
   */
  public String getRegion() {
    return region;
  }

  /**
   * @param region the region to set
   */
  public void setRegion(String region) {
    this.region = region;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if (getLang() != null) {
      if (getRegion() != null) {
        return getLang() + "_" + getRegion();
      }
      return getLang();
    }
    return "";
  }

}
