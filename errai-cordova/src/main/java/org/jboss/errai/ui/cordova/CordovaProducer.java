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
