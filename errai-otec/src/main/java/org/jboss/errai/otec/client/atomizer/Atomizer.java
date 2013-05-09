/*
 * Copyright 2013 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.otec.client.atomizer;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ValueBoxBase;
import org.jboss.errai.otec.client.ListenerRegistration;
import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OTEntity;
import org.jboss.errai.otec.client.StateChangeListener;
import org.jboss.errai.otec.client.util.DiffUtil;

import java.util.Collection;

/**
 * @author Mike Brock
 * @author Christian Sadilek
 */
public abstract class Atomizer {
  private Atomizer() {
  }

  public static AtomizerSession syncWidgetWith(final OTEngine engine, final OTEntity entity, final ValueBoxBase widget) {

    final Multimap<Object, HandlerRegistration> HANDLER_REGISTRATION_MAP
        = HashMultimap.create();
    final EntityChangeStreamImpl entityChangeStream = new EntityChangeStreamImpl(engine, entity);
    engine.getPeerState().addEntityStream(entityChangeStream);

    widget.setValue(entity.getState().get());

    HANDLER_REGISTRATION_MAP.put(widget, widget.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(final KeyDownEvent event) {
        if (shouldIgnoreKeyPress(event)) {
          return;
        }

        if (widget.getSelectedText().length() > 0) {
          entityChangeStream.notifyDelete(widget.getCursorPos(), widget.getSelectedText());
        }
        else if (event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE) {
          final int index = widget.getCursorPos() - 1;
          entityChangeStream.notifyDelete(index, String.valueOf(widget.getText().charAt(index)));
        }
        else if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          entityChangeStream.notifyInsert(widget.getCursorPos(), "\n");
        }
      }
    }));

    HANDLER_REGISTRATION_MAP.put(widget, widget.addKeyPressHandler(new KeyPressHandler() {
      @Override
      public void onKeyPress(final KeyPressEvent event) {
        if (event.getUnicodeCharCode() != 13 && event.getUnicodeCharCode() != 0) {
          entityChangeStream.notifyInsert(widget.getCursorPos(), String.valueOf(event.getCharCode()));
        }
      }
    }));

    DOM.setEventListener(widget.getElement(), new EventListener() {
      @Override
      public void onBrowserEvent(Event event) {
        if (event.getTypeInt() == Event.ONPASTE) {
          final String before = (String) entity.getState().get();
          new Timer() {
            @Override
            public void run() {
              final String after = (String) widget.getValue();
              final DiffUtil.Delta diff = DiffUtil.diff(before, after);

              entityChangeStream.notifyInsert(diff.getCursor(), diff.getDeltaText());
            }
          }.schedule(1);
        }
        widget.onBrowserEvent(event);
      }
    });

    attachCutHandler(widget.getElement(), new Runnable() {
      @Override
      public void run() {
        entityChangeStream.notifyDelete(widget.getCursorPos(), widget.getSelectedText());
      }
    });

    attachTextDragHandler(widget.getElement(), new Runnable() {
          @Override
          public void run() {
            entityChangeStream.notifyDelete(widget.getCursorPos(), widget.getSelectedText());
            entityChangeStream.flush();
          }
        },
        new Runnable() {
          @Override
          public void run() {
            final String old = (String) entity.getState().get();
            new Timer() {
              @Override
              public void run() {
                final DiffUtil.Delta diff = DiffUtil.diff(old, (String) widget.getValue());
                if (diff.getDeltaText().length() > 0) {
                  entityChangeStream.notifyInsert(diff.getCursor(), diff.getDeltaText());
                }
              }
            }.schedule(1);
          }
        }
    );

    final ListenerRegistration listenerRegistration
        = entity.getState().addStateChangeListener(new StateChangeListener() {
      @Override
      public int getCursorPos() {
        return widget.getCursorPos();
      }

      @Override
      public void onStateChange(final int newCursorPos, final Object newValue) {
        widget.setEnabled(false);
        widget.setValue(newValue);
        final int length = String.valueOf(newValue).length();
        if (length >= newCursorPos) {
          widget.setCursorPos(newCursorPos);
        }
        else {
          widget.setCursorPos(length);
        }
        widget.setEnabled(true);
      }
    });

    DOM.sinkEvents(widget.getElement(), DOM.getEventsSunk(widget.getElement()) | Event.ONPASTE);

    final Timer timer = new Timer() {
      @Override
      public void run() {
        entityChangeStream.flush();
      }
    };
    timer.scheduleRepeating(500);

    return new AtomizerSession() {
      @Override
      public void end() {
        listenerRegistration.remove();
        final Collection<HandlerRegistration> values = HANDLER_REGISTRATION_MAP.values();
        for (final HandlerRegistration value : values) {
          value.removeHandler();
        }
        timer.cancel();
      }
    };
  }

  private static boolean shouldIgnoreKeyPress(KeyEvent event) {
    if (event.isMetaKeyDown() || event.isControlKeyDown()) {
      return true;
    }

    int keyCode;
    if (event instanceof KeyDownEvent) {
      keyCode = ((KeyDownEvent) event).getNativeKeyCode();
    }
    else {
      return true;
    }

    switch (keyCode) {
      case KeyCodes.KEY_DOWN:
      case KeyCodes.KEY_LEFT:
      case KeyCodes.KEY_UP:
      case KeyCodes.KEY_RIGHT:
      case KeyCodes.KEY_ESCAPE:
      case KeyCodes.KEY_PAGEDOWN:
      case KeyCodes.KEY_PAGEUP:
      case KeyCodes.KEY_HOME:
      case KeyCodes.KEY_END:
        return true;
    }

    return false;
  }

  private static native void attachCutHandler(Element element, Runnable runnable) /*-{
      element.oncut = function () {
          runnable.@java.lang.Runnable::run()();
          return true;
      }


  }-*/;

  private static native void attachTextDragHandler(Element element, Runnable onStart, Runnable onFinish) /*-{

      element.ondragstart = function () {
          onStart.@java.lang.Runnable::run()();
          return true;
      }


      element.ondragend = function () {
          onFinish.@java.lang.Runnable::run()();
          return true;
      }


  }-*/;

}
