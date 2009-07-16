package org.jboss.workspace.sampler.client.filebrowser;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.core.client.GWT;
import org.jboss.workspace.client.layout.WorkPanel;

import java.io.File;

public class FileBrowserWidget extends Composite {
    TextArea fileList = new TextArea();
    String currentDir = ".";
    Label currentDirectory = new Label();

    FileBrowserAsync svc = (FileBrowserAsync) GWT.create(FileBrowser.class);
    ServiceDefTarget svcTarget = (ServiceDefTarget) svc;    

    AsyncCallback callback = new AsyncCallback() {
        public void onFailure(Throwable caught) {
            Window.alert(caught.getMessage());
        }

        public void onSuccess(Object result) {
            setFileList((String) result);
            setLabelText();
        }
    };

    public FileBrowserWidget() {
        WorkPanel panel = new WorkPanel();
        panel.addToTitlebar(new HTML("File Browser"));

        VerticalPanel p = new VerticalPanel();
        p.setWidth("100%");

        SimplePanel titleArea = new SimplePanel();
        titleArea.setWidth("100%");
        titleArea.getElement().getStyle().setProperty("background", "#fff799");
        titleArea.add(new Label("This console allows you to browse the current and parent directories."));

        p.add(titleArea);
        panel.add(p);

        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.setSize("100%", "250px");

        VerticalPanel vpanel = new VerticalPanel();
        vpanel.setSize("90%", "200px");

        vpanel.add(currentDirectory);

        p.add(hPanel);

        fileList.setSize("80%","150px");
        fileList.setReadOnly(true);
        svcTarget.setServiceEntryPoint(GWT.getModuleBaseURL()+"fileBrowser");
        svc.getFiles(currentDir, callback);
        vpanel.add(fileList);

        HorizontalPanel hp = new HorizontalPanel();
        hp.add(createPrevButton());
        hp.add(createCurrentButton());
        setLabelText();

        vpanel.add(hp);

        hPanel.add(vpanel);
        hPanel.setCellHorizontalAlignment(vpanel, HasHorizontalAlignment.ALIGN_CENTER);
        hPanel.setCellVerticalAlignment(vpanel, HasVerticalAlignment.ALIGN_MIDDLE);


        initWidget(panel);
    }

    private void setLabelText() {
        AsyncCallback currentDirCallback = new AsyncCallback() {
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            public void onSuccess(Object result) {
                currentDirectory.setText("Path: " + (String) result);
            }
        };

        svc.getCurrentDir(currentDirCallback);
    }

    private Button createPrevButton() {
        final Button button = new Button("Show parent directory");

        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                currentDir += "/..";
                svc.getFiles(currentDir, callback);
            }
        });

        return button;
    }

    private Button createCurrentButton() {
        final Button button = new Button("Go back to current directory");

        button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                currentDir = ".";
                svc.getFiles(currentDir, callback);
            }
        });

        return button;
    }

    private void setFileList(String filelist) {
        if (filelist.equals(""))
            filelist = "Directory is empty";
        fileList.setText(filelist);
    }
}