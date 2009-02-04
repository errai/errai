package org.jboss.workspace.client.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import static com.google.gwt.user.client.Event.ONMOUSEUP;
import static com.google.gwt.user.client.Event.ONMOUSEDOWN;
import static com.google.gwt.user.client.Event.ONMOUSEOUT;
import static com.google.gwt.user.client.Event.ONMOUSEOVER;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import org.jboss.workspace.client.util.Effects;

import java.util.ArrayList;
import java.util.List;


public class WSLaunchButton extends HTML {
    private static final String CSS_NAME = "WSLaunchButton";
    private static final String CSS_NAME_DOWN = "WSLaunchButton-down";
    private static final String CSS_NAME_HOVER = "WSLaunchButton-hover";
    private static final String CSS_NAME_MOVE = "WSLaunchButton-move";

    private Image icon;
    private String name;

    private AnimationTimer animTimer;

    private List<ClickListener> clickListeners;

    public WSLaunchButton(Image icon, String name) {
        super();

        this.icon = icon;

        this.name = name;

        sinkEvents(Event.MOUSEEVENTS);

        setHTML(createButtonMarkup());

        addStyleName(CSS_NAME);
    }


    @Override
    public void onBrowserEvent(Event event) {
        if (!isAttached()) return;

        if (animTimer == null) {
            Element el = getElement();
            if (el == null) return;
            Element parent = el.getParentElement();
            if (parent == null) return;

            animTimer = new AnimationTimer(parent.getStyle());
        }

        switch (event.getTypeInt()) {
            case ONMOUSEOVER:
                setStyleName(CSS_NAME_HOVER);

                if (animTimer.isRunning()) {
                    animTimer.cancel();
                }
                animTimer.setUp(true);
                animTimer.scheduleRepeating(10);

                break;
            case ONMOUSEOUT:
                setStyleName(CSS_NAME);

                break;
            case ONMOUSEDOWN:
                setStyleName(CSS_NAME_DOWN);

                break;
            case ONMOUSEUP:
                if (clickListeners != null) {
                    for (ClickListener listen : clickListeners) {
                        listen.onClick(this);
                    }
                }
                animTimer.setRunning(false);

                setStyleName(CSS_NAME);
                break;
        }

    }

    private String createButtonMarkup() {
        return "<span class=\"" + CSS_NAME + "-contents\"> <img class=\"" + CSS_NAME + "-contents\" src=\"" + icon.getUrl() + "\" width=\"16\" height=\"16\" style=\"padding-right:2px; padding-left:2px;\"/>" +
                name + "</span>";
    }

    public void setName(String name) {
        setHTML(createButtonMarkup());
    }

    private void setIcon(Image icon) {
        setHTML(createButtonMarkup());
    }

    public Image getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public static class AnimationTimer extends Timer {
        float i = 0.1f;
        float step = 0.02f;

        boolean up = true;
        boolean running = true;
        boolean _running = true;

        Style s;

        public AnimationTimer(Style s) {
            this.s = s;
        }

        public void run() {
            if (up) {
                i += step;
                if (i >= 1.0f) {
                    i = 1.0f;
                    running = _running = false;
                }
            }
            else {
                i -= step;
                if (i <= 0.5f) {
                    i = 0.1f;
                    running = _running = false;
                }
            }

            Effects.setOpacity(s, i);
            if (!_running) cancel();
        }

        public void schedule(int i) {
            this.running = this._running = true;
            super.schedule(i);
        }

        public void scheduleRepeating(int i) {
            this.running = this._running = true;
            super.scheduleRepeating(i);
        }

        public boolean isUp() {
            return up;
        }

        public void setUp(boolean up) {
            if (this.up = up) {
                i = 0.1f;
            }
            else {
                i = 1.0f;
            }
        }

        public void setUp(boolean up, float fadepoint) {
            this.up = up;
            i = fadepoint;
        }


        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }
    }

    public void addClickListener(ClickListener listener) {
        if (clickListeners == null) clickListeners = new ArrayList<ClickListener>();
        clickListeners.add(listener);
    }
}
