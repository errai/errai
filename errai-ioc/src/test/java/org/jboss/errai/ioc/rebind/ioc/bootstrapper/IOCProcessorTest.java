package org.jboss.errai.ioc.rebind.ioc.bootstrapper;

import org.jboss.errai.ioc.client.api.EnabledByProperty;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.class)
public class IOCProcessorTest {

    private static final String SYSTEM_PROPERTY_NAME = "errai.test";

    @Mock
    private InjectionContext context;

    @InjectMocks
    private IOCProcessor processor;

    @Test
    public void testGetPropertyValue(){
        assertFalse(processor.getPropertyValue(SYSTEM_PROPERTY_NAME,
                                               "true",
                                               ""));
        assertTrue(processor.getPropertyValue(SYSTEM_PROPERTY_NAME,
                                              "true",
                                              "true"));
        assertTrue(processor.getPropertyValue(SYSTEM_PROPERTY_NAME,
                                              "test",
                                              "test"));
        assertFalse(processor.getPropertyValue(SYSTEM_PROPERTY_NAME,
                                               "false",
                                               "true"));
        System.setProperty(SYSTEM_PROPERTY_NAME, "true");
        assertTrue(processor.getPropertyValue(SYSTEM_PROPERTY_NAME,
                                              "true",
                                              ""));
        assertTrue(processor.getPropertyValue(SYSTEM_PROPERTY_NAME,
                                               "true",
                                               "false"));
        System.setProperty(SYSTEM_PROPERTY_NAME, "false");
        assertFalse(processor.getPropertyValue(SYSTEM_PROPERTY_NAME,
                                               "true",
                                               ""));
        assertFalse(processor.getPropertyValue(SYSTEM_PROPERTY_NAME,
                                               "true",
                                               "true"));
        assertFalse(processor.getPropertyValue(SYSTEM_PROPERTY_NAME,
                                               "test",
                                               "true"));
        System.setProperty(SYSTEM_PROPERTY_NAME, "test");
        assertTrue(processor.getPropertyValue(SYSTEM_PROPERTY_NAME,
                                              "test",
                                              "true"));
    }


}
