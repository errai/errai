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
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

/**
 * @author edewit@redhat.com
 */
public class CordovaResources {

  public static void configure() {
    Resources res = Resources.RESOURCES;
    String platform = determinePlatform();
    TextResource javascript;

    if ("android".equals(platform)) {
      javascript = res.cordovaAndroid();
    } else if ("ios".equals(platform)) {
      javascript = res.cordovaIOS();
    } else if ("blackberry".equals(platform)) {
      javascript = res.cordovaBlackberry();
    } else if ("window7".equals(platform)) {
      javascript = res.cordovaWindows7();
    } else if ("window8".equals(platform)) {
      javascript = res.cordovaWindows8();
    } else {
      javascript = res.cordovaIOS();
    }

    JavascriptInjector.inject(javascript.getText());
  }

  private static native String determinePlatform() /*-{
    return (function () {
      var ua = window.navigator.userAgent.toLowerCase();

      if (ua.indexOf('android') != -1) {
        return "android";
      }

      if (ua.indexOf('iphone') != -1 || ua.indexOf('ipod') != -1) {
        return "ios";
      }

      if (ua.indexOf('blackberry') != -1) {
        return "blackberry";
      }

      if (ua.indexOf('windows phone') != -1) {
        if (ua.indexOf('7.') != -1) {
          return "windows7"
        } else {
          return "windows8"
        }
      }

      return "desktop";

    })();
  }-*/;

  public static interface Resources extends ClientBundle {
    public static Resources RESOURCES = GWT.create(Resources.class);

    @Source("js/cordova-android-3.4.0.js")
    TextResource cordovaAndroid();

    @Source("js/cordova-blackberry-3.4.0.js")
    TextResource cordovaBlackberry();

    @Source("js/cordova-ios-3.4.0.js")
    TextResource cordovaIOS();

    @Source("js/cordova-windows7-3.4.0.js")
    TextResource cordovaWindows7();

    @Source("js/cordova-windows8-3.4.0.js")
    TextResource cordovaWindows8();
  }
}
