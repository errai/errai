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

package org.jboss.errai.forge.facet.ui.command.res;

import org.jboss.forge.addon.ui.UIProvider;
import org.jboss.forge.addon.ui.command.CommandExecutionListener;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UISelection;
import org.jboss.forge.addon.ui.input.UIPrompt;
import org.jboss.forge.addon.ui.progress.UIProgressMonitor;
import org.jboss.forge.furnace.spi.ListenerRegistration;

import javax.enterprise.inject.Alternative;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Alternative
public class UIExecutionContextMock implements UIExecutionContext {

  @Override
  public UIContext getUIContext() {
    return new UIContext() {
      @Override
      public void close() throws Exception {
      }
      
      @Override
      public <SELECTIONTYPE> void setSelection(SELECTIONTYPE resource) {
      }

      @Override
      public <SELECTIONTYPE> void setSelection(UISelection<SELECTIONTYPE> resource) {
      }

      @Override
      public <SELECTIONTYPE> UISelection<SELECTIONTYPE> getSelection() {
        return null;
      }
      
      @Override
      public UIProvider getProvider() {
        return null;
      }
      
      @Override
      public Set<CommandExecutionListener> getListeners() {
        return null;
      }
      
      @Override
      public <SELECTIONTYPE> UISelection<SELECTIONTYPE> getInitialSelection() {
        return new UISelection<SELECTIONTYPE>() {
          @Override
          public Iterator<SELECTIONTYPE> iterator() {
            return Collections.emptyIterator();
          }

          @Override
          public SELECTIONTYPE get() {
            return null;
          }

          @Override
          public int size() {
            return 0;
          }

          @Override
          public boolean isEmpty() {
            return true;
          }
          
        };
      }
      
      @Override
      public Map<Object, Object> getAttributeMap() {
        return null;
      }
      
      @Override
      public ListenerRegistration<CommandExecutionListener> addCommandExecutionListener(CommandExecutionListener listener) {
        return null;
      }
    };
  }

  @Override
  public UIProgressMonitor getProgressMonitor() {
    return null;
  }

  @Override
  public UIPrompt getPrompt() {
    return null;
  }

}
