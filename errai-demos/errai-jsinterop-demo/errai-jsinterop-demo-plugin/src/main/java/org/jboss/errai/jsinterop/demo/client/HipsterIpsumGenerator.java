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

package org.jboss.errai.jsinterop.demo.client;

import javax.inject.Singleton;

import org.jboss.errai.jsinterop.demo.client.IpsumGenerator;

import jsinterop.annotations.JsType;

@JsType
@Singleton
public class HipsterIpsumGenerator implements IpsumGenerator {

  private static final String TEXT =
          "Polaroid letterpress vinyl quinoa, VHS raw denim everyday carry mlkshk venmo man braid pork belly organic."
          + "Banjo kogi health goth messenger bag, roof party swag brooklyn keffiyeh craft beer heirloom."
          + "Pitchfork vice drinking vinegar, portland tousled offal brunch migas mustache hammock asymmetrical fanny pack chicharrones YOLO vegan."
          + "Brunch meh celiac, fap gastropub farm-to-table gentrify microdosing tofu migas stumptown YOLO."
          + "Bushwick fingerstache direct trade, PBR&B truffaut you probably haven't heard of them lumbersexual"
          + "health goth polaroid banh mi offal biodiesel tote bag chambray."
          + "Chambray ramps chillwave gastropub normcore, literally yuccie ennui seitan photo booth waistcoat.";

  @Override
  public String getId() {
    return "hipster-ipsum";
  }

  @Override
  public String getName() {
    return "Hipster Ipsum";
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
    return "Generates hipster ipsum filler text, courtesy of <a href='http://hipsum.co/'>hipsum.co</a>.";
  }

}
