package org.jboss.errai.forge.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.jboss.errai.GwtMockitoRunnerExtension;

import org.jboss.errai.forge.facet.ui.command.res.SimpleTestableClass;

@RunWith(GwtMockitoRunnerExtension.class)
public class SimpleTestClass {

  @InjectMocks
  private SimpleTestableClass testableInstance;

  @Test
  public void test() throws Exception {
    fail("Not implemented");
  }

}
