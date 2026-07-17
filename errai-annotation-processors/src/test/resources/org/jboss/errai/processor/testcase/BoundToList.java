package org.jboss.errai.processor.testcase;

import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;

import com.google.gwt.user.client.TakesValue;

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