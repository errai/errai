/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.demo.grocery.client.local;

import java.util.List;

import javax.enterprise.context.Dependent;

import org.jboss.errai.demo.grocery.client.shared.Department;
import org.jboss.errai.ioc.client.api.LoadAsync;
import org.jboss.errai.ui.client.widget.ListWidget;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;

/**
 * A list of Department objects (each represented by a DepartmentWidget) whose entries can be dragged to rearrange their order.
 * <p>
 * It should be easy at some point in the future to generalize this and include it alongside ListWidget in the framework.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Dependent
@LoadAsync
public class DepartmentList extends ListWidget<Department, DepartmentWidget> {

    /**
     * When an entry in this list is currently being dragged by the user, this field refers to it. When nothing is being
     * dragged, this field is null.
     */
    private DepartmentWidget draggingDepartmentWidget;

    @Override
    protected Class<DepartmentWidget> getItemComponentType() {
        return DepartmentWidget.class;
    }

    /**
     * Sets the list of model objects that should be represented in the list. This widget supports rearrangement of its items by
     * drag-and-drop. The rearrangement is performed on the given list object, so be sure of two things:
     * <ol>
     * <li>The given item list must be mutable
     * <li>If you want to save the list in its rearranged state, you have to retain a reference to the list and persist it in
     * its new order when this list widget has been disposed (or any other time you like)
     * </ol>
     *
     * @param items The list of items to display in the list. This list must support the add() and remove() operations, or
     *        drag-and-drop operations will fail.
     */
    @Override
    public void setItems(final List<Department> items) {
        super.setItems(items);
    }

    /**
     * Adding drag and drop support to all rendered item widgets.
     */
    @Override
    protected void onItemsRendered(final List<Department> items) {
        // make all the widgets draggable
        for (int i = 0; i < getPanel().getWidgetCount(); i++) {
            final int widgetIndex = i;
            final DepartmentWidget dw = getComponent(widgetIndex);
            dw.getElement().getStyle().setPaddingRight(20, Unit.PX);
            final ItemMoveAnimation growAnimation = new ItemMoveAnimation(dw);
            dw.getElement().setDraggable(Element.DRAGGABLE_TRUE);
            dw.addDragStartHandler(new DragStartHandler() {
                @Override
                public void onDragStart(DragStartEvent event) {
                    draggingDepartmentWidget = dw;
                    event.setData("text", dw.getModel().getName());
                    event.getDataTransfer().setDragImage(dw.getElement(), 10, 10);
                    dw.getElement().getStyle().setColor("#ddd");
                }
            });
            dw.addBitlessDomHandler(new DragEnterHandler() {
                @Override
                public void onDragEnter(DragEnterEvent event) {
                    if (draggingDepartmentWidget == null)
                        return; // some foreign object must be dragging over
                    if (draggingDepartmentWidget == dw)
                        return; // don't try to drag the widget onto itself!
                    growAnimation.forward(draggingDepartmentWidget.getOffsetHeight());
                }
            }, DragEnterEvent.getType());
            dw.addBitlessDomHandler(new DragOverHandler() {
                @Override
                public void onDragOver(DragOverEvent event) {
                    // we need to observe DragOver events, or we will not get a drop event from the browser
                }
            }, DragOverEvent.getType());
            dw.addBitlessDomHandler(new DragLeaveHandler() {
                @Override
                public void onDragLeave(DragLeaveEvent event) {
                    growAnimation.reverse();
                }
            }, DragLeaveEvent.getType());
            dw.addBitlessDomHandler(new DropHandler() {
                @Override
                public void onDrop(DropEvent event) {
                    event.preventDefault();
                    growAnimation.reverse();

                    int indexOfDraggingWidget = -1;
                    for (int i = 0; i < getPanel().getWidgetCount(); i++) {
                        if (draggingDepartmentWidget == getPanel().getWidget(i)) {
                            indexOfDraggingWidget = i;
                            break;
                        }
                    }

                    // remove first then add. if done the other way around, indices > widgetIndex become incorrect
                    items.remove(indexOfDraggingWidget);
                    int addIndex = widgetIndex;
                    items.add(addIndex, draggingDepartmentWidget.getModel());
                    onItemsRendered(items);

                }
            }, DropEvent.getType());


        }
    }

    /**
     * Animates the creation and destruction of a gap above a certain item in the list.
     *
     * @author Jonathan Fuerth <jfuerth@redhat.com>
     */
    class ItemMoveAnimation extends Animation {

        private final DepartmentWidget item;
        private double start;
        private double current;
        private double end;

        public ItemMoveAnimation(DepartmentWidget item) {
            this.item = item;
        }

        @Override
        protected void onUpdate(double progress) {
            current = start + ((end - start) * progress);

        }

        @Override
        protected void onComplete() {
            current = end;
            item.getElement().getStyle().setPaddingTop(current, Unit.PX);
            super.onComplete();
        }

        /**
         * Runs this animation forward, to make the widget grow empty space above its contents.
         *
         * @param targetGrowth the amount to grow by once the animation has finished.
         */
        public void forward(double targetGrowth) {
            start = 0;
            end = targetGrowth;
            run(400);
        }

        /**
         * Runs this animation in reverse, to make the widget shrink to its normal size. Safe to call any time: whether the
         * animation is already running, has finished running, or was never run in the first place.
         */
        public void reverse() {
            start = current;
            end = 0;

            // don't run the animation if we're already collapsed!
            if (current != 0) {
                run(400);
            }
        }
    }

}
