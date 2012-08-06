/*
 * Copyright 2012 JBoss, a division of Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.databinding.client;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.ObjectUtils;

/**
 *
 * @author David Cracauer <dcracauer@gmail.com>
 */
public class PropertyChangeHandlerSupport {
    
    private List<PropertyChangeHandler> handlers = new ArrayList<PropertyChangeHandler>();
    
    /**
     * 
     * Adds a {@link  PropertyChangeHandler} to the list of handlers.
     * @param handler the {@link PropertyChangeHandler} to add.
     */
    public void addPropertyChangeHandler(PropertyChangeHandler handler){
        handlers.add(handler);
    }
    /**
     * 
     * Removes a {@link  PropertyChangeHandler} from the list of handlers.
     * @param handler the {@link PropertyChangeHandler} to remove.
     */
    public void removePropertyChangeHandler(PropertyChangeHandler handler){
        handlers.remove(handler);
    }
    
    /**
     * Notify registered {@link PropertyChangeHandlers} of a {@link PropertyChangeEvent}.
     * Will only dispatch events that represent a change; If oldValue and newValue are equal, the event will be ignored.
     * @param event {@link the PropertyChangeEvent} to provide to handlers.
     */
    public void notifyHandlers(PropertyChangeEvent event){
        if(!acceptEvent(event)){
            return;
        }
    
        for(PropertyChangeHandler handler : handlers){
            handler.onPropertyChange(event);
        }
    }
    
    private boolean acceptEvent(PropertyChangeEvent event){
        if(event == null){
            return false;
        }
        
        if(event.getOldValue() == null){
            return event.getNewValue() != null;
        }
        
        if(event.getNewValue()==null){
            return event.getOldValue() != null;
        }
        
        return !event.getOldValue().equals(event.getNewValue());
        
    }
    
}
