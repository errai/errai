package org.jboss.workspace.sampler.client.filebrowser;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.core.client.GWT;
import org.jboss.workspace.client.layout.WorkPanel;

public class FileBrowserWidget extends Composite {
    TextArea fileList = new TextArea();

    public FileBrowserWidget() {
        WorkPanel panel = new WorkPanel();
        panel.addToTitlebar(new HTML("File Browser"));

        VerticalPanel p = new VerticalPanel();
        p.setWidth("100%");

        SimplePanel titleArea = new SimplePanel();
        titleArea.setWidth("100%");
        titleArea.getElement().getStyle().setProperty("background", "#fff799");
        titleArea.add(new Label("This console allows you to browse the files in the current directory."));

        p.add(titleArea);
        panel.add(p);

        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.setSize("100%", "250px");

        VerticalPanel vpanel = new VerticalPanel();
        vpanel.setSize("90%", "200px");

        p.add(hPanel);

        fileList.setSize("80%","150px");
        fileList.setReadOnly(true);
        vpanel.add(fileList);
        vpanel.add(createShowButton());

        hPanel.add(vpanel);
        hPanel.setCellHorizontalAlignment(vpanel, HasHorizontalAlignment.ALIGN_CENTER);
        hPanel.setCellVerticalAlignment(vpanel, HasVerticalAlignment.ALIGN_MIDDLE);


        initWidget(panel);
    }

    private Button createShowButton() {
        final Button button = new Button("Show files");

        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                button.setEnabled(false);

                FileBrowserAsync svc = (FileBrowserAsync) GWT.create(FileBrowser.class);
                ServiceDefTarget svcTarget = (ServiceDefTarget) svc;
                svcTarget.setServiceEntryPoint(GWT.getModuleBaseURL() + "fileBrowser");

                AsyncCallback callback = new AsyncCallback() {
                    public void onFailure(Throwable caught) {
                        caught.printStackTrace();
                        Window.alert(caught.getMessage());
                    }

                    public void onSuccess(Object result) {
                        button.setEnabled(true);

                        String filelist = (String) result;
                        if (filelist.equals(""))
                            filelist = "Directory is empty";
                        fileList.setText(filelist);
                    }
                };

                svc.getFiles(callback);
            }
        });

        return button;
    }
}