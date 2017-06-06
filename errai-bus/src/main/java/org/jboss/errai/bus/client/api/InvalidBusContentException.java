/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.client.api;

public class InvalidBusContentException extends RuntimeException {
  private final String contentType;
  private final String content;

  public InvalidBusContentException(String actualContentType, String content) {
    super("Invalid bus content! Expecting content type 'application/json', but got '" + actualContentType + '!');
    this.contentType = actualContentType;
    this.content = content;
  }

  public String getContentType() {
    return contentType;
  }

  public String getContent() {
    return content;
  }
}
