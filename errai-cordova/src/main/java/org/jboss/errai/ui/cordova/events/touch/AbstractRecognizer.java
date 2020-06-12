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

package org.jboss.errai.ui.cordova.events.touch;

import com.google.gwt.event.dom.client.TouchCancelHandler;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartHandler;

/**
 * @author edewit@redhat.com
 */
public abstract class AbstractRecognizer implements TouchStartHandler, TouchMoveHandler, TouchEndHandler, TouchCancelHandler {

  public enum State {
    INVALID, READY, FINDER_DOWN, FINGERS_DOWN, WAITING, FINGERS_UP, ONE_FINGER, TWO_FINGER, FOUND_DIRECTION
  }
}
