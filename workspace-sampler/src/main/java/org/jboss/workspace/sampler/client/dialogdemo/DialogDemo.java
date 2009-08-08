package org.jboss.workspace.sampler.client.dialogdemo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Window;
import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.framework.Tool;
import org.jboss.workspace.client.rpc.MessageBusClient;
import org.jboss.workspace.client.layout.WorkPanel;
import org.jboss.workspace.client.widgets.WSModalDialog;

import java.util.Map;

public class DialogDemo implements Tool {

    public Widget getWidget() {
        WorkPanel panel = new WorkPanel();
        panel.setTitle("Dialog Demo");

        DockPanel dPanel = new DockPanel();
        dPanel.setWidth("100%");
        dPanel.setHeight("250px");

        panel.add(dPanel);

        Button b = createButton();

        dPanel.add(b, DockPanel.CENTER);
        dPanel.setCellHorizontalAlignment(b, HasHorizontalAlignment.ALIGN_CENTER);

        MessageBusClient.subscribe("mysubject", panel.getElement(), new AcceptsCallback() {
            public void callback(Object message, Object data) {
                Map m = (Map) MessageBusClient.decodeMap(message);

                Window.alert("I see you just opened a component (" + m.get("Component") + "): " + m.get("Opened"));

            }
        }, null);

        return panel;
    }

    private Button createButton() {
        Button button = new Button("Show Dialog");
        final WSModalDialog dialog = new WSModalDialog("Hello World!");

        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                dialog.showModal();
            }
        });

        dialog.getOkButton().setText("Ok?");
        dialog.getCancelButton().setText("No thanks!");

        dialog.ask("Would you like to continue?", new AcceptsCallback() {
            public void callback(Object message, Object data) {
            }
        });

        return button;
    }

    public String getName() {
        return "Dialog Demo";
    }

    public String getId() {
        return "dialogDemo";
    }

    public Image getIcon() {
        return new Image(GWT.getModuleBaseURL() + "/images/ui/icons/application.png");
    }

    public boolean multipleAllowed() {
        return true;
    }
}
