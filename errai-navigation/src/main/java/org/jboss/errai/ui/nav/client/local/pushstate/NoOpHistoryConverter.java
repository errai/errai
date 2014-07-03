package org.jboss.errai.ui.nav.client.local.pushstate;

import de.barop.gwt.client.HistoryConverter;
/**
 * Used when Errai PushState is disabled or unavailable in the current browser.
 * @author Max Barkley <mbarkley@redhat.com>
 * @author Divya Dadlani <ddadlani@redhat.com>
 *
 */
public class NoOpHistoryConverter implements HistoryConverter {

  @Override
  public void convertHistoryToken() {}

}
