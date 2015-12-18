/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.errai.samples.i18ndemo.client.local;

import org.jboss.errai.ui.shared.api.annotations.TranslationKey;

/**
 * Some extra translation keys for use by the app.
 * 
 * @author eric.wittmann@redhat.com
 */
public class AppMessages {

  @TranslationKey(defaultValue = "Message One Value")
  public static final String MESSAGE_1 = "translatable.messages.message-1";

  @TranslationKey(defaultValue = "Message {0} Value")
  public static final String MESSAGE_2 = "translatable.messages.message-2";

}
