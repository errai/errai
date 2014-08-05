package org.jboss.errai.ui.nav.rebind;

import org.jboss.errai.ui.nav.client.local.DefaultNavigationErrorHandler;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.UniquePageRole;
import org.jboss.errai.ui.nav.client.local.spi.NavigationGraph;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * 
 * @author Divya Dadlani <ddadlani@redhat.com>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class NavigationErrorTest {
  
  @InjectMocks
  private DefaultNavigationErrorHandler errorHandler;
  
  @Mock
  private Navigation navigation;
  
  @Mock
  private NavigationGraph navGraph;
  
  @Mock
  private PageNode<IsWidget> defaultPageNode;
  
  @Mock
  private PageNode<IsWidget> notDefaultPageNode;
  
  @Test(expected=Error.class)
  public void testErrorHandlingOnDefaultPageUsingPageName() {
    errorHandler.handleInvalidPageNameError(new Exception("test for navigation"), "");
  }
  
  @Test(expected=Error.class)
  public void testErrorHandlingOnDefaultPageUsingPageRole() {
    errorHandler.handleError(new Exception("test for navigation"), DefaultPage.class);
  }
  
  @Test(expected=Error.class)
  public void testErrorHandlingOnDefaultPageUsingURLPath() {
    errorHandler.handleInvalidURLError(new Exception("test for navigation"), "");
  }
  
  @Test
  public void testRedirectToDefaultPageUsingPageName() {
    errorHandler.handleInvalidPageNameError(new Exception("test navigation redirect"), "notDefaultPageNode");
    Mockito.verify(navigation).goTo("");
  }

  @Test
  public void testRedirectToDefaultPageUsingPageRole() {
    errorHandler.handleError(new Exception("test navigation redirect"), TestRole.class);
    Mockito.verify(navigation).goTo("");
  }
  
  @Test
  public void testRedirectToDefaultPageUsingURLPath() {
    errorHandler.handleInvalidURLError(new Exception("test navigation redirect"), "TestRole");
    Mockito.verify(navigation).goTo("");
  }
  
  static interface TestRole extends UniquePageRole {
    
  }
}
