package org.jboss.workspace.sampler.client.servicecontrol;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.*;
import org.jboss.workspace.client.layout.WorkPanel;


public class ServiceWidget extends Composite {
    WorkPanel panel = new WorkPanel();

    Image statusImage;
    HTML statusText;

    private String status = "running";

    public ServiceWidget() {
        panel.addToTitlebar(new HTML("Service Control"));

        VerticalPanel p = new VerticalPanel();
        p.setWidth("100%");

        SimplePanel titleArea = new SimplePanel();
        titleArea.setWidth("100%");
        titleArea.getElement().getStyle().setProperty("background", "#fff799");
        titleArea.add(new Label("This console allows you to control the Widget Service."));

        p.add(titleArea);
        panel.add(p);

        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.setWidth("100%");
        hPanel.setHeight("250px");
        DockPanel dPanel = new DockPanel();
        dPanel.setWidth("80%");

        Style dStyle = dPanel.getElement().getStyle();
        dStyle.setProperty("border", "1px solid #A1A1A1");
        dStyle.setProperty("background", "white");

        p.add(hPanel);

        statusImage = new Image("images/wsdemo/traffic-green.png");

        dPanel.add(statusImage, DockPanel.WEST);

        VerticalPanel innerPanel = new VerticalPanel();

        statusText = new HTML("The widget service is currently <strong>running</strong>");

        innerPanel.add(statusText);

        HorizontalPanel buttons = new HorizontalPanel();
        buttons.add(createStartButton());
        buttons.add(createPauseButton());
        buttons.add(createStopButton());

        innerPanel.add(buttons);

        dPanel.add(innerPanel, DockPanel.CENTER);
        dPanel.setCellVerticalAlignment(innerPanel, HasVerticalAlignment.ALIGN_MIDDLE);

        hPanel.add(dPanel);
        hPanel.setCellHorizontalAlignment(dPanel, HasHorizontalAlignment.ALIGN_CENTER);
        hPanel.setCellVerticalAlignment(dPanel, HasVerticalAlignment.ALIGN_MIDDLE);


        initWidget(panel);
    }

    private Button createStartButton() {
        final Button button = new Button("Start");

        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                button.setEnabled(false);

                ServiceControlAsync svc = (ServiceControlAsync) GWT.create(ServiceControl.class);
                ServiceDefTarget svcTarget = (ServiceDefTarget) svc;
                svcTarget.setServiceEntryPoint(GWT.getModuleBaseURL() + "serviceControl");

                AsyncCallback callback = new AsyncCallback() {
                    public void onFailure(Throwable caught) {
                        Window.alert("FAIL!");
                    }

                    public void onSuccess(Object result) {
                        getStatus();
                        button.setEnabled(true);
                    }
                };

                svc.startService(callback);
            }
        });

        return button;
    }

    private Button createPauseButton() {
        final Button button = new Button("Pause");

        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                button.setEnabled(false);

                ServiceControlAsync svc = (ServiceControlAsync) GWT.create(ServiceControl.class);
                ServiceDefTarget svcTarget = (ServiceDefTarget) svc;
                svcTarget.setServiceEntryPoint(GWT.getModuleBaseURL() + "serviceControl");

                AsyncCallback callback = new AsyncCallback() {
                    public void onFailure(Throwable caught) {

                    }

                    public void onSuccess(Object result) {
                        getStatus();
                        button.setEnabled(true);

                    }
                };

                svc.pauseService(callback);
            }
        });

        return button;
    }

    private Button createStopButton() {
        final Button button = new Button("Stop");

        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                button.setEnabled(false);

                ServiceControlAsync svc = (ServiceControlAsync) GWT.create(ServiceControl.class);
                ServiceDefTarget svcTarget = (ServiceDefTarget) svc;
                svcTarget.setServiceEntryPoint(GWT.getModuleBaseURL() + "serviceControl");

                AsyncCallback callback = new AsyncCallback() {
                    public void onFailure(Throwable caught) {

                    }

                    public void onSuccess(Object result) {
                        getStatus();
                        button.setEnabled(true);

                    }
                };

                svc.stopService(callback);
            }
        });

        return button;
    }

    private void getStatus() {
        ServiceControlAsync svc = (ServiceControlAsync) GWT.create(ServiceControl.class);
        ServiceDefTarget svcTarget = (ServiceDefTarget) svc;
        svcTarget.setServiceEntryPoint(GWT.getModuleBaseURL() + "serviceControl");

        AsyncCallback cb = new AsyncCallback() {
            public void onFailure(Throwable caught) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void onSuccess(Object result) {
                updateStatus((Integer) result);
            }
        };

        svc.getServiceStatus(cb);
    }

    private void updateStatus(int statusCode) {


        String imgSuffix;
        switch (statusCode) {
            case ServiceControl.STOPPED:
                imgSuffix = "red";
                statusText.setHTML("The widget service is currently <strong>stopped</strong>");
                break;
            case ServiceControl.PAUSED:
                imgSuffix = "yellow";
                statusText.setHTML("The widget service is currently <strong>paused</strong>");
                break;
            case ServiceControl.STARTED:
                imgSuffix = "green";
                statusText.setHTML("The widget service is currently <strong>running</strong>");
                break;
            default:
                imgSuffix = "red";
                statusText.setHTML("The widget service is currently <strong>unknown</strong>");

        }

        String url = GWT.getHostPageBaseURL() + "images/wsdemo/traffic-" + imgSuffix + ".png";

        statusImage.setUrl(url);
    }

}
