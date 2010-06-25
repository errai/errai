package org.errai.samples.helloworld.client;

import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.errai.ioc.client.api.CreatePanel;
import org.jboss.errai.ioc.client.api.ToRootPanel;


@CreatePanel @ToRootPanel
public class MyPanel extends VerticalPanel {
    public MyPanel() {
        super();
    }
}
