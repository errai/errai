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

import com.google.gwt.dom.client.Touch;

/**
 * Represents the touch of the user.
 * 
 * @author edewit@redhat.com
 */
public class TouchPoint {

  private final int id;
  private final int x;
  private final int y;

  public TouchPoint(int id, int x, int y) {
    this.id = id;
    this.x = x;
    this.y = y;
  }

  public TouchPoint(Touch touch) {
    this.id = touch.getIdentifier();
    this.x = touch.getPageX();
    this.y = touch.getPageY();
  }

  public int getId() {
    return id;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }
}
