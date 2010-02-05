/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.workspaces.client.icons;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * Icons that ship as part of the workspace framework.
 * See {@link com.google.gwt.user.client.ui.ImageBundle} for further explanations.
 *
 * @author Heiko Braun <hbraun@redhat.com>
 */
public interface ErraiImageBundle extends ClientBundle {
    @Source("message_icon.png")
    public ImageResource messageIcon();

    @Source("workspacelogo.png")
    public ImageResource workspaceLogo();

    @Source("collapseleft.png")
    public ImageResource collapseLeft();

    @Source("application.png")
    public ImageResource application();

    @Source("collapseright.png")
    public ImageResource collapseRight();

    @Source("close-icon.png")
    public ImageResource closeIcon();

    @Source("questioncube.png")
    public ImageResource questionCube();

    @Source("user.png")
    public ImageResource user();
}
