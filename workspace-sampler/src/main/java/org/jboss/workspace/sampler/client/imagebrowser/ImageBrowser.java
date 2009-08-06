package org.jboss.workspace.sampler.client.imagebrowser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import org.jboss.workspace.client.framework.Tool;
import org.jboss.workspace.client.rpc.StatePacket;
import org.jboss.workspace.client.widgets.WSWindowPanel;


public class ImageBrowser implements Tool {
    public Widget getWidget() {
        return new Composite() {
            DockPanel dockPanel = new DockPanel();

            {
                HorizontalPanel toolBar = new HorizontalPanel();
                toolBar.setSize("100%", "100%");

                toolBar.add(new Label("Image Browser"));
                toolBar.setStyleName("WSToolbar");

                dockPanel.add(toolBar, DockPanel.NORTH);
                dockPanel.setCellHeight(toolBar, "20px");
                dockPanel.setCellWidth(toolBar, "100%");

                ScrollPanel viewArea = new ScrollPanel();

                viewArea.setSize("100%", "100%");
                viewArea.setAlwaysShowScrollBars(true);
                dockPanel.add(viewArea, DockPanel.CENTER);

                /**
                 * The actual thumbnail viewers panel.
                 */

                FlowPanel thumbs = new FlowPanel();
                thumbs.setWidth("100%");
                viewArea.add(thumbs);

                thumbs.add(decorateImage(GWT.getModuleBaseURL() + "/imagebrowser/toronto1.jpg"));
                thumbs.add(decorateImage(GWT.getModuleBaseURL() + "/imagebrowser/toronto2.jpg"));


                dockPanel.setSize("100%", "100%");
                initWidget(dockPanel);
            }

        };
    }

    public String getName() {
        return "Image Browser";
    }

    public String getId() {
        return "imageBrowser";
    }

    public Image getIcon() {
        return new Image(GWT.getModuleBaseURL() + "/images/ui/icons/camera_go.png");
    }

    public boolean multipleAllowed() {
        return true;
    }

    private Image decorateImage(final String imageURL) {

        final Image i = new Image();

        i.addLoadHandler(new LoadHandler() {
            public void onLoad(LoadEvent event) {

                int width = i.getWidth();
                int height = i.getHeight();

                i.setHeight(dimRatio(width, 150, height) + "px");
                i.setWidth("150px");

                i.setVisible(true);

                i.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        final WSWindowPanel panel = new WSWindowPanel();


                        final SimplePanel containerPanel = new SimplePanel();
                        final Image wImg = new Image();

                        wImg.addLoadHandler(new LoadHandler() {
                            public void onLoad(LoadEvent event) {

                                panel.show();

                                int height = wImg.getElement().getOffsetHeight();
                                int width = wImg.getElement().getOffsetWidth();

                                if (height == 0 || width == 0) {
                                    Window.alert("Image has no width/height!");
                                }

                                int windowHeight = Window.getClientHeight();
                                int windowWidth = Window.getClientWidth();

                                int newHeight = (int) Math.round(windowHeight * 0.8);
                                int newWidth = (int) Math.round(windowWidth * 0.8);

                                double ratio;
                                if (height > newHeight) {
                                    ratio = ((double) newHeight) / ((double) height);

                                    height = newHeight;
                                    width = (int) Math.round(width * ratio);
                                }
                                if (width > newWidth) {
                                    ratio = ((double) newWidth) / ((double) width);
                                    width = newWidth;
                                    height = (int) Math.round(height * ratio);
                                }

                                containerPanel.setSize(width + "px", height + "px");
                                wImg.setSize(width + "px", height + "px");
                                panel.center();

                            }
                        });
                        wImg.getElement().getStyle().setProperty("margin", "5px;");

                        containerPanel.add(wImg);
                        panel.add(containerPanel);

                        /**
                         * In order to deal with caching problems in IE, we don't set the URL for the
                         * image until after we've finished building up the DOM.  
                         */
                        DeferredCommand.addCommand(new Command() {
                            public void execute() {
                                wImg.setUrl(i.getUrl());
                                panel.setTitle("Viewing Image: " + (wImg.getUrl().substring(wImg.getUrl().lastIndexOf('/')+1))
                                        + " (" + wImg.getWidth() + "x" + wImg.getHeight() + ")");


                            }
                        });

                    }
                });

            }
        });


        DeferredCommand.addCommand(new Command() {
            public void execute() {
                i.setUrl(imageURL);
            }
        });

        Style s = i.getElement().getStyle();
        s.setProperty("border", "1px solid gray");
        s.setProperty("margin", "2px");


        return i;
    }


    private int dimRatio(double value, double newValue, double sourceValue) {
        return (int) Math.round(sourceValue * (newValue / value));
    }
}
