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
package org.jboss.errai.databinding.client.test;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import org.jboss.errai.databinding.client.PropertyChangeEvent;
import org.jboss.errai.databinding.client.PropertyChangeHandler;
import org.jboss.errai.databinding.client.PropertyChangeHandlerSupport;
import org.junit.*;

/**
 *
 * @author dcracauer
 */
public class PropertyChangeHandlerSupportTest {
    
    private PropertyChangeHandlerSupport support;
    
    @Before
    public void setUp() {
        support = new PropertyChangeHandlerSupport();
    }
    
    @Test
    public void testAdd(){
        
        MockHandler mh1 = new MockHandler( );
        support.addPropertyChangeHandler(mh1);
        
        PropertyChangeEvent event1 = new PropertyChangeEvent("foo", 1, 2);
        
        support.notifyHandlers(event1);
        assertEquals(1, mh1.events.size());
        assertEquals(event1, mh1.events.get(0));
        
        MockHandler mh2 = new MockHandler( );
        
        support.addPropertyChangeHandler(mh2);
        
        PropertyChangeEvent event2 = new PropertyChangeEvent("foo", 2, 3);
        
        support.notifyHandlers(event2);
        assertEquals(2, mh1.events.size());
        assertEquals(event2, mh1.events.get(1));
        assertEquals(1, mh2.events.size());
        assertEquals(event2, mh2.events.get(0));
        
    }
    
      
    @Test
    public void testRemove(){
        MockHandler mh1 = new MockHandler( );
        support.addPropertyChangeHandler(mh1);
        
        PropertyChangeEvent event1 = new PropertyChangeEvent("foo", 1, 2);
        support.notifyHandlers(event1);
        assertEquals(1, mh1.events.size());
        assertEquals(event1, mh1.events.get(0));
        
        support.removePropertyChangeHandler(mh1);
        
        mh1.events.clear();;
        
        PropertyChangeEvent event2 = new PropertyChangeEvent("foo", 2, 3);
        
        support.notifyHandlers(event2);
        assertEquals(0, mh1.events.size());
    }
    
    
    @Test
    public void testNoChange(){
        MockHandler mh1 = new MockHandler( );
        support.addPropertyChangeHandler(mh1);
        
        support.notifyHandlers(new PropertyChangeEvent("foo", null, null));
        support.notifyHandlers(new PropertyChangeEvent("foo", 1, 1));
        support.notifyHandlers(new PropertyChangeEvent("foo", "test", "test"));
        assertEquals(0, mh1.events.size());
       
       
    }
}
