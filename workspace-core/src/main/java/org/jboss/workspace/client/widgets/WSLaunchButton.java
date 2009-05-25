package org.jboss.workspace.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Event;
import static com.google.gwt.user.client.Event.*;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import org.jboss.workspace.client.util.Effects;

import java.util.ArrayList;
import java.util.List;


public class WSLaunchButton extends Composite {
    private static final String CSS_NAME = "WSLaunchButton";
    private static final String CSS_NAME_DOWN = "WSLaunchButton-down";
    private static final String CSS_NAME_HOVER = "WSLaunchButton-hover";
    private static final String CSS_NAME_MOVE = "WSLaunchButton-move";

    private Image icon;
    private String name;
    private SimplePanel panel = new SimplePanel();

    private List<ClickListener> clickListeners;

    public WSLaunchButton(Image icon, String name) {
        super();

        this.icon = icon;
        this.name = name;

        sinkEvents(Event.MOUSEEVENTS);

        panel.add(new HTML(createButtonMarkup()));
        panel.setStylePrimaryName(CSS_NAME);

//        setHeight("20px");
//        setWidth("100%");

        initWidget(panel);


    }


    @Override
    public void onBrowserEvent(Event event) {
        if (!isAttached()) return;

        switch (event.getTypeInt()) {
            case ONMOUSEOVER:
                addStyleDependentName("hover");

                Effects.fade(getElement(), 1, 2, 20, 100);

                break;
            case ONMOUSEOUT:
                removeStyleDependentName("hover");

                break;
            case ONMOUSEDOWN:
                addStyleDependentName("down");
                break;
            case ONMOUSEUP:
                if (clickListeners != null) {
                    for (ClickListener listen : clickListeners) {
                        listen.onClick(this);
                    }
                }
                //animTimer.setRunning(false);

                setStyleName(CSS_NAME);
                break;
        }

    }

    private String createButtonMarkup() {
        return "<span class=\"" + CSS_NAME + "-contents\"> <img class=\"" + CSS_NAME + "-contents\" src=\"" + icon.getUrl() + "\" width=\"16\" height=\"16\" style=\"padding-right:2px; padding-left:2px;\"/>" +
                name + "</span>";
    }

//    public void setName(String name) {
//        setHTML(createButtonMarkup());
//    }
//
//    private void setIcon(Image icon) {
//        setHTML(createButtonMarkup());
//    }

    public Image getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public void addClickListener(ClickListener listener) {
        if (clickListeners == null) clickListeners = new ArrayList<ClickListener>();
        clickListeners.add(listener);
    }
}
