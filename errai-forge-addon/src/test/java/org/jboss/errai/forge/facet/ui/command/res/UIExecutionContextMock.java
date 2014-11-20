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
