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

package org.jboss.errai.ui.cordova.events.touch.mock;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.TouchStartEvent;

import static org.mockito.Mockito.*;

public class MockTouchStartEvent extends TouchStartEvent {

  private JsArray<Touch> touches;

  private Touch touch;

  public MockTouchStartEvent(int id, int x, int y) {
    touches = mock(JsArray.class);
    touch = mock(Touch.class);
    when(touches.get(0)).thenReturn(touch);
    when(touch.getIdentifier()).thenReturn(id);
    when(touch.getPageX()).thenReturn(x);
    when(touch.getPageY()).thenReturn(y);
  }

  public MockTouchStartEvent(int id1, int x1, int y1, int id2, int x2, int y2) {
    this(id1, x1, y1);

    final Touch touch1 = mock(Touch.class);
    when(touches.get(1)).thenReturn(touch1);
    when(touch1.getIdentifier()).thenReturn(id2);
    when(touch1.getPageX()).thenReturn(x2);
    when(touch1.getPageY()).thenReturn(y2);
  }

  @Override
  public JsArray<Touch> getTouches() {
    return touches;
  }
}
