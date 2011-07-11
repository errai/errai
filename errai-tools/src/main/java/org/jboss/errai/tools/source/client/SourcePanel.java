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
package org.jboss.errai.tools.source.client;

import com.google.gwt.user.client.ui.HTML;
import org.gwt.mosaic.ui.client.ScrollLayoutPanel;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: May 18, 2010
 */
public class SourcePanel extends ScrollLayoutPanel {
  private HTML html;

  public SourcePanel() {
    super();

    html = new HTML();

    this.add(html);
  }

  public void setSource(String source) {
    html.setHTML(source);
  }
}
