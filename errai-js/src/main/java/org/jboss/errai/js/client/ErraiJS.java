/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.js.client;

import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.js.client.bus.MsgBus;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

/**
 * @author Mike Brock
 */
public class ErraiJS implements EntryPoint {
  @Override
  public void onModuleLoad() {
    GWT.create(MsgBus.class);
    erraiOnLoad();
    
    CDI.addPostInitTask(new Runnable(){
      @Override
      public void run() {
        erraiCdiOnLoad();
      }
    });
  }

  private native void erraiOnLoad() /*-{
    $wnd.erraiTypeOf = function (value) {
        var s = typeof value;
        if (s === 'object') {
            if (value) {
                if (typeof value.length === 'number' &&
                        !(value.propertyIsEnumerable('length')) &&
                        typeof value.splice === 'function') {
                    s = 'array';
                }
            } else {
                s = 'null';
            }
        }
        return s;
    };

    if ($wnd.erraiOnLoad && typeof $wnd.erraiOnLoad == 'function') $wnd.erraiOnLoad();
  }-*/;
  
  private native void erraiCdiOnLoad() /*-{
    if ($wnd.erraiCdiOnLoad && typeof $wnd.erraiCdiOnLoad == 'function') $wnd.erraiCdiOnLoad();
  }-*/;
}
