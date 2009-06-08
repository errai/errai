package org.jboss.workspace.client.layout;

import com.google.gwt.user.client.ui.*;

public class WSDropShadowLayout extends Composite {
    private DockPanel dockPanel = new DockPanel();

    public WSDropShadowLayout(Widget wrappedWidget) {
        HorizontalPanel top = new HorizontalPanel();
        top.setWidth("100%");
        dockPanel.add(top, DockPanel.NORTH);
        dockPanel.setCellWidth(top, "100%");

        SimplePanel topLeftCorner = new SimplePanel();
        topLeftCorner.setStyleName("WSDropShadow-topLeftCorner");
        top.add(topLeftCorner);

        SimplePanel topLeft = new SimplePanel();
        topLeft.setStyleName("WSDropShadow-topLeft");
        top.add(topLeft);

        SimplePanel topMiddle = new SimplePanel();
        topMiddle.setStyleName("WSDropShadow-topMiddle");
        top.add(topMiddle);
        top.setCellWidth(topMiddle, "100%");

        SimplePanel topRight = new SimplePanel();
        topRight.setStyleName("WSDropShadow-topRight");
        top.add(topRight);

        SimplePanel topRightCorner = new SimplePanel();
        topRightCorner.setStyleName("WSDropShadow-topRightCorner");
        top.add(topRightCorner);

        VerticalPanel left = new VerticalPanel();
        dockPanel.add(left, DockPanel.WEST);

        SimplePanel leftTop = new SimplePanel();
        leftTop.setStyleName("WSDropShadow-leftTop");
        left.add(leftTop);

        SimplePanel leftMiddle = new SimplePanel();
        leftMiddle.setStyleName("WSDropShadow-leftMiddle");
        left.add(leftMiddle);

        SimplePanel leftBottom = new SimplePanel();
        leftBottom.setStyleName("WSDropShadow-leftBottom");
        left.add(leftBottom);

        dockPanel.add(wrappedWidget, DockPanel.CENTER);

        VerticalPanel right = new VerticalPanel();
        dockPanel.add(right, DockPanel.EAST);

        SimplePanel rightTop = new SimplePanel();
        rightTop.setStyleName("WSDropShadow-rightTop");
        right.add(rightTop);

        SimplePanel rightMiddle = new SimplePanel();
        rightMiddle.setStyleName("WSDropShadow-rightMiddle");
        right.add(rightMiddle);

        SimplePanel rightBottom = new SimplePanel();
        rightBottom.setStyleName("WSDropShadow-rightBottom");
        right.add(rightBottom);

        HorizontalPanel bottom = new HorizontalPanel();
        dockPanel.add(bottom, DockPanel.SOUTH);

        SimplePanel bottomLeftCorner = new SimplePanel();
        bottomLeftCorner.setStyleName("WSDropShadow-bottomLeftCorner");
        bottom.add(bottomLeftCorner);

        SimplePanel bottomLeft = new SimplePanel();
        bottomLeft.setStyleName("WSDropShadow-bottomLeft");
        bottom.add(bottomLeft);

        SimplePanel bottomMiddle = new SimplePanel();
        bottomMiddle.setStyleName("WSDropShadow-bottomMiddle");
        bottom.add(bottomMiddle);

        SimplePanel bottomRight = new SimplePanel();
        bottomRight.setStyleName("WSDropShadow-bottomRight");
        bottom.add(bottomRight);

        SimplePanel bottomRightCorner = new SimplePanel();
        bottomRightCorner.setStyleName("WSDropShadow-bottomRightCorner");
        bottom.add(bottomRightCorner);

        initWidget(dockPanel);
    }
}
