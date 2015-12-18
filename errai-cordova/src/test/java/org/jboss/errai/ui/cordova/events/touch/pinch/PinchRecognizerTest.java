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

package org.jboss.errai.ui.cordova.events.touch.pinch;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.jboss.errai.ui.cordova.events.touch.mock.MockHasHandlers;
import org.jboss.errai.ui.cordova.events.touch.mock.MockTouchMoveEvent;
import org.jboss.errai.ui.cordova.events.touch.mock.MockTouchStartEvent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author edewit@redhat.com
 */
@RunWith(GwtMockitoTestRunner.class)
public class PinchRecognizerTest {

  private MockHasHandlers hasHandlers = new MockHasHandlers();
  private PinchRecognizer pinchRecognizer = new PinchRecognizer(hasHandlers, new OffsetProvider() {
    @Override
    public int getLeft() {
      return 0;
    }

    @Override
    public int getTop() {
      return 0;
    }
  });

  @Test
  public void testSimplePinch() {
    //when
    pinchRecognizer.onTouchStart(new MockTouchStartEvent(1, 0, 0));
    pinchRecognizer.onTouchStart(new MockTouchStartEvent(1, 0, 0, 2, 100, 100));
    pinchRecognizer.onTouchMove(new MockTouchMoveEvent(1, 0, 0, 2, 50, 50));

    //then
    GwtEvent<?> event = hasHandlers.getEvent();

    if (!(event instanceof PinchEvent)) {
      Assert.fail("no pinch event");
    }
    PinchEvent pinchEvent = (PinchEvent) event;

    assertEquals(25, pinchEvent.getX());
    assertEquals(25, pinchEvent.getY());

    assertEquals(1.41421356237309, pinchEvent.getScaleFactor(), 0.0001);
  }

}
