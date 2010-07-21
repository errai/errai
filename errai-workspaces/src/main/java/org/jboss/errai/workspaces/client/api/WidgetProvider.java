/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.workspaces.client.api;

/**
 * Main interface for workspace tools.
 * A workspace instance demands a widget from a provider when necessary.
 * The callback allows for both synchronous and asynchronous creation of widgets
 * when needed.<p>
 * A typical pattern, leveraging the GWT 2.0 code spitting features looks like this:
 *
 * <pre>
 *
 * AT LoadTool(name="MyTool", group="MyToolsetGroup")
 * class MyModule implements WidgetProvider {
 *
 * private static Widget instance = null;
 *  
 * public void provideWidget(final ProvisioningCallback callback)
 * {
 *   GWT.runAsync(
 *       new RunAsyncCallback()
 *       {
 *         public void onFailure(Throwable err)
 *         {
 *           GWT.log("Failed to load tool", err);
 *         }
 *
 *         public void onSuccess()
 *         {
 *           if (null == instance) {
 *             instance = new HTML(""); // create your widget here
 *           }
 *           callback.onSuccess(instance);
 *         }
 *       }
 *
 *   );
 * }
 * }
 * </pre>
 */
public interface WidgetProvider {
    public void provideWidget(ProvisioningCallback callback);
}
