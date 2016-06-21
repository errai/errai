/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.jsinterop.demo.client.local;

import javax.inject.Singleton;

import org.jboss.errai.jsinterop.demo.client.IpsumGenerator;

@Singleton
public class LoremIpsumGenerator implements IpsumGenerator {

  public final String TEXT =
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
          + "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
          + "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur."
          + "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

  @Override
  public String getId() {
    return "lorem-ipsum";
  }

  @Override
  public String getName() {
    return "Lorem Ipsum";
  }

  @Override
  public String[] generateIpsum(final int paragraphNumber) {
    final String[] paragraphs = new String[paragraphNumber];
    for (int i = 0; i < paragraphNumber; i++) {
      paragraphs[i] = TEXT;
    }

    return paragraphs;
  }

  @Override
  public String getDescription() {
    return "Generates boring, old lorem ipsum text.";
  }

}
