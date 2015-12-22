/*
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

package org.jboss.errai.ui.nav.client.local.api;


/**
 * This exception is thrown when the page has caused more than the maximum number of redirects (
 * see {@link org.jboss.errai.ui.nav.client.local.Navigation}), indicating an infinite redirection loop.
 *
 * @author Divya Dadlani <ddadlani@redhat.com>
 *
 */
public class RedirectLoopException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public RedirectLoopException() {

  }

  public RedirectLoopException(String message) {
    super(message);
  }

  public RedirectLoopException(Throwable cause) {
    super(cause);
  }

  public RedirectLoopException(String message, Throwable cause) {
    super(message, cause);
  }

}
