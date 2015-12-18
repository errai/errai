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

package org.jboss.errai.processor;

/**
 * A collection of fully qualified type names that are of interest to our
 * annotation processors. Because Eclipse does not like annotation processors to
 * use types defined in the same workspace where they are being used, and
 * because the javax.lang.mirror API works mostly on strings rather than runtime
 * Class objects, we define all the interesting type names from GWT and Errai in
 * here as string constants.
 *
 * @author jfuerth
 *
 */
public class TypeNames {

  static final String GWT_WIDGET = "com.google.gwt.user.client.ui.Widget";
  static final String GWT_ELEMENT = "com.google.gwt.dom.client.Element";
  static final String GWT_COMPOSITE = "com.google.gwt.user.client.ui.Composite";
  static final String GWT_OPAQUE_DOM_EVENT = "com.google.gwt.user.client.Event";
  static final String GWT_EVENT = "com.google.gwt.event.shared.GwtEvent";
  static final String JS_TYPE = "jsinterop.annotations.JsType";

  static final String JAVAX_INJECT = "javax.inject.Inject";

  static final String TEMPLATED = "org.jboss.errai.ui.shared.api.annotations.Templated";
  static final String DATA_FIELD = "org.jboss.errai.ui.shared.api.annotations.DataField";
  static final String EVENT_HANDLER = "org.jboss.errai.ui.shared.api.annotations.EventHandler";
  static final String SINK_NATIVE = "org.jboss.errai.ui.shared.api.annotations.SinkNative";

  static final String BOUND = "org.jboss.errai.ui.shared.api.annotations.Bound";
  static final String AUTO_BOUND = "org.jboss.errai.ui.shared.api.annotations.AutoBound";
  static final String MODEL = "org.jboss.errai.ui.shared.api.annotations.Model";

}
