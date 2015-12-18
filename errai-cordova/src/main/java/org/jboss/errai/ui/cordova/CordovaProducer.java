package org.jboss.errai.ui.cordova;

import com.google.gwt.core.client.GWT;
import com.googlecode.gwtphonegap.client.PhoneGap;
import com.googlecode.gwtphonegap.client.PhoneGapAvailableEvent;
import com.googlecode.gwtphonegap.client.PhoneGapAvailableHandler;
import org.jboss.errai.ioc.client.api.InitBallot;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Wrapper around gwtphonegap so we can inject the phonegap capabilities in a more errai way
 */
@Singleton
public class CordovaProducer {
  @Inject
  InitBallot<CordovaProducer> ballot;

  private PhoneGap phoneGap;

  public PhoneGap getPhoneGap() {
    if (phoneGap == null) {
      CordovaResources.configure();
      phoneGap = GWT.create(PhoneGap.class);
      phoneGap.addHandler(new PhoneGapAvailableHandler() {
        @Override
        public void onPhoneGapAvailable(PhoneGapAvailableEvent event) {
          ballot.voteForInit();
        }
      });

      phoneGap.initializePhoneGap();
    }

    return phoneGap;
  }
}