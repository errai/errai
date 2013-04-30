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
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ValueBoxBase;
import org.jboss.errai.otec.client.OTEngine;
import org.jboss.errai.otec.client.OTEntity;

/**
 * @author Mike Brock
 */
public abstract class Atomizer {
  private Atomizer() {
  }

  private static final Multimap<Object, HandlerRegistration> HANDLER_REGISTRATION_MAP
      = HashMultimap.create();

  public static void syncWidgetWith(final OTEngine engine, final OTEntity entity, final ValueBoxBase widget) {

    final EntityChangeStreamImpl entityChangeStream = new EntityChangeStreamImpl(engine, entity);

    widget.setValue(entity.getState().get());

    HANDLER_REGISTRATION_MAP.put(widget, widget.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(final KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE) {
          final int index = widget.getCursorPos() - 1;
          entityChangeStream.notifyDelete(index, String.valueOf(widget.getText().charAt(index)));
        }
      }
    }));

    HANDLER_REGISTRATION_MAP.put(widget, widget.addKeyPressHandler(new KeyPressHandler() {
      @Override
      public void onKeyPress(final KeyPressEvent event) {
        if (event.getUnicodeCharCode() != 0) {
          final char charCode = event.getCharCode();
          entityChangeStream.notifyInsert(widget.getCursorPos(), String.valueOf(charCode));
        }
      }
    }));

    new Timer() {
      @Override
      public void run() {
        entityChangeStream.flush();
      }
    }.scheduleRepeating(750);
  }

//  private static boolean isControlCharacter(final int charCode) {
//    return charCode < 0x20 || (charCode >= 0x7F && charCode < 0xA0);
//  }
}
