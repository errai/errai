package org.jboss.errai.ui.nav.client.local.pushstate;

import de.barop.gwt.client.HistoryConverter;
import de.barop.gwt.client.HistoryConverterHash;
import de.barop.gwt.client.HistoryConverterPushState;

/**
 * This implementation wraps either a {@link HistoryConverterPushState} or the {@link HistoryConverterHash}. At runtime, if HTML5
 * pushstate is supported, the former implementation is used.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 * @author Divya Dadlani <ddadlani@redhat.com
 *
 */
public class DynamicHistoryConverter implements HistoryConverter {

  private HistoryConverter historyConverter;

  public DynamicHistoryConverter() {
    if (PushStateUtil.isPushStateActivated())
      historyConverter = new HistoryConverterPushState();
    else
      historyConverter = new NoOpHistoryConverter();
  }

  @Override
  public void convertHistoryToken() {
    historyConverter.convertHistoryToken();

  }

}
