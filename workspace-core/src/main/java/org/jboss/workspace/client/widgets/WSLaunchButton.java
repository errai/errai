package org.jboss.workspace.client.widgets;

import com.google.gwt.user.client.Event;
import static com.google.gwt.user.client.Event.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import org.jboss.workspace.client.util.Effects;

import java.util.ArrayList;
import java.util.List;


public class WSLaunchButton extends Composite {
    private static final String CSS_NAME = "WSLaunchButton";

    private Image icon;
    private String name;
    private SimplePanel panel = new SimplePanel();

    private List<ClickHandler> clickHandlers;

    public WSLaunchButton(Image icon, String name) {
        super();

        this.icon = icon;
        this.name = name;

        sinkEvents(Event.MOUSEEVENTS);

        panel.add(new HTML(createButtonMarkup()));
        panel.setStylePrimaryName(CSS_NAME);

        initWidget(panel);
    }


    @Override
    public void onBrowserEvent(Event event) {                               
        if (!isAttached()) {
            return;
        }

        switch (event.getTypeInt()) {
            case ONMOUSEMOVE:
                break;
            case ONMOUSEOVER:
                addStyleDependentName("hover");
                break;
            case ONBLUR:
            case ONLOSECAPTURE:
            case ONMOUSEOUT:
                removeStyleDependentName("hover");
                break;
            case ONMOUSEDOWN:
                addStyleDependentName("down");
                break;
            case ONMOUSEUP:
                if (clickHandlers != null) {
                    for (ClickHandler listen : clickHandlers) {
                        listen.onClick(null);
                    }
                }
                setStyleName(CSS_NAME);
                break;
        }

    }

    private String createButtonMarkup() {
        return "<span class=\"" + CSS_NAME + "-contents\"> <img class=\"" + CSS_NAME + "-contents\" src=\"" + icon.getUrl() + "\" width=\"16\" height=\"16\" style=\"padding-right:2px; padding-left:2px;\"/>" +
                name + "</span>";
    }

    public Image getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public void addClickListener(ClickHandler handler) {
        if (clickHandlers == null) clickHandlers = new ArrayList<ClickHandler>();
        clickHandlers.add(handler);
    }
}
