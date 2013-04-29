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

package org.jboss.errai.otec.gwt;

import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import org.jboss.errai.otec.atomizer.Atomizer;
import org.jboss.errai.otec.atomizer.OTHandler;

/**
 * @author Mike Brock
 */
public class KeyPressOTHandler implements OTHandler, KeyPressHandler {
  private final HasKeyPressHandlers widget;
  private final Atomizer atomizer;

  public KeyPressOTHandler(HasKeyPressHandlers widget, Atomizer atomizer) {
    this.widget = widget;
    this.atomizer = atomizer;
    widget.addKeyPressHandler(this);
  }

  private void configureHandler() {

  }

  @Override
  public void onKeyPress(KeyPressEvent event) {

  }
}
