package org.jboss.errai.ui.nav.client.local.pushstate;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.impl.HistoryImpl;

import de.barop.gwt.client.HistoryImplPushState;

/**
 * This implementation wraps either a {@link HistoryImplPushState} or the {@link HistoryImpl}. At runtime, if HTML5
 * pushstate is supported, the former implementation is used.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 * @author Divya Dadlani <ddadlani@redhat.com
 *
 */
public class DynamicHistoryImpl extends HistoryImplPushState {

  private HistoryImpl historyImpl;
  
  public DynamicHistoryImpl() {
    if(PushStateUtil.isPushStateActivated()) {
      historyImpl = new HistoryImplPushState();
    }
    else
      historyImpl = new HistoryImpl();
  }
  
  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return historyImpl.addValueChangeHandler(handler);
  }

  @Override
  public void dispose() {
    historyImpl.dispose();
  }

  @Override
  public String encodeFragment(String fragment) {
    return historyImpl.encodeFragment(fragment);
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
    historyImpl.fireEvent(event);
  }

  @Override
  public void fireHistoryChangedImpl(String newToken) {
    historyImpl.fireHistoryChangedImpl(newToken);
  }

  @Override
  public HandlerManager getHandlers() {
    return historyImpl.getHandlers();
  }

  @Override
  public boolean init() {
    return historyImpl.init();
  }

  @Override
  protected native String decodeFragment(String encodedFragment) /*-{
   var impl = this.@org.jboss.errai.ui.nav.client.local.pushstate.DynamicHistoryImpl::historyImpl;
   return impl.@com.google.gwt.user.client.impl.HistoryImpl::decodeFragment(Ljava/lang/String;)(encodedFragment); 
  }-*/;

  @Override
  protected native void nativeUpdate(String historyToken) /*-{
  var impl = this.@org.jboss.errai.ui.nav.client.local.pushstate.DynamicHistoryImpl::historyImpl;
  return impl.@com.google.gwt.user.client.impl.HistoryImpl::nativeUpdate(Ljava/lang/String;)(historyToken); 
 }-*/;

  @Override
  protected native void nativeUpdateOnEvent(String historyToken) /*-{
  var impl = this.@org.jboss.errai.ui.nav.client.local.pushstate.DynamicHistoryImpl::historyImpl;
  return impl.@com.google.gwt.user.client.impl.HistoryImpl::nativeUpdateOnEvent(Ljava/lang/String;)(historyToken); 
 }-*/;
  
}
