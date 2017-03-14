package org.jboss.errai.processor.testcase;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.Model;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

@Dependent
public class BoundToList {
    @Inject @AutoBound
    private DataBinder<List<BoundModelClass>> binder;

    @Inject @Bound
    private ListComponent<BoundModelClass, ItemView> list;
    
    public static class ItemView implements TakesValue<BoundModelClass> {
      
      public void setValue(BoundModelClass model) {}
      
      public BoundModelClass getValue() {
        return null;
      }
      
    }
}