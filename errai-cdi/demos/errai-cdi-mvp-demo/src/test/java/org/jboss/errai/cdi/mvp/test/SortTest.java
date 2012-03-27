package org.jboss.errai.cdi.mvp.test;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.cdi.demo.mvp.client.presenter.ContactsPresenter;
import org.jboss.errai.cdi.demo.mvp.client.presenter.EditContactPresenter;
import org.jboss.errai.cdi.demo.mvp.client.view.ContactsView;
import org.jboss.errai.cdi.demo.mvp.client.view.EditContactView;
import org.jboss.errai.enterprise.client.cdi.EventProvider;
import org.jboss.errai.enterprise.client.cdi.InstanceProvider;
import org.jboss.errai.ioc.client.api.Caller;
import org.jboss.errai.ioc.client.api.builtin.CallerProvider;
import org.jboss.errai.ioc.client.api.builtin.IOCBeanManagerProvider;
import org.jboss.errai.ioc.client.api.builtin.MessageBusProvider;
import org.jboss.errai.ioc.client.api.builtin.RequestDispatcherProvider;
import org.jboss.errai.ioc.client.api.builtin.RootPanelProvider;
import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.jboss.errai.ioc.rebind.ioc.graph.SortUnit;

import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Arrays.sort;
import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.get;

/**
 * @author Mike Brock
 */
public class SortTest {
  /**
   * INFO] PRINTING IOC GRAPH
   [INFO] SortUnit: (depth:0)org.jboss.errai.ioc.client.api.builtin.MessageBusProvider => []
   [INFO] ProcessingDelegate:org.jboss.errai.ioc.client.api.builtin.MessageBusProvider
   [INFO] SortUnit: (depth:0)org.jboss.errai.enterprise.client.cdi.EventProvider => []
   [INFO] ProcessingDelegate:org.jboss.errai.enterprise.client.cdi.EventProvider
   [INFO] SortUnit: (depth:0)org.jboss.errai.ioc.client.api.builtin.RootPanelProvider => []
   [INFO] ProcessingDelegate:org.jboss.errai.ioc.client.api.builtin.RootPanelProvider
   [INFO] SortUnit: (depth:0)org.jboss.errai.ioc.client.api.builtin.RequestDispatcherProvider => []
   [INFO] ProcessingDelegate:org.jboss.errai.ioc.client.api.builtin.RequestDispatcherProvider
   [INFO] SortUnit: (depth:0)org.jboss.errai.ioc.client.api.builtin.CallerProvider => []
   [INFO] ProcessingDelegate:org.jboss.errai.ioc.client.api.builtin.CallerProvider
   [INFO] SortUnit: (depth:0)org.jboss.errai.ioc.client.api.builtin.IOCBeanManagerProvider => []
   [INFO] ProcessingDelegate:org.jboss.errai.ioc.client.api.builtin.IOCBeanManagerProvider
   [INFO] SortUnit: (depth:0)org.jboss.errai.enterprise.client.cdi.InstanceProvider => []
   [INFO] ProcessingDelegate:org.jboss.errai.enterprise.client.cdi.InstanceProvider
   [INFO] SortUnit: (depth:2)org.jboss.errai.demo.client.presenter.EditContactPresenter => [ (depth:0)org.jboss.errai.ioc.client.api.Caller => [],  (depth:0)org.jboss.errai.demo.client.view.EditContactView => [],  (depth:0)org.jboss.errai.demo.client.presenter.EditContactPresenter$Display => [],  (depth:1)com.google.gwt.event.shared.HandlerManager => [ (depth:0)org.jboss.errai.demo.client.Contacts => []],  (depth:0)org.jboss.errai.ioc.client.api.builtin.CallerProvider$1 => []]
   [INFO] ProcessingDelegate:org.jboss.errai.demo.client.presenter.EditContactPresenter
   [INFO] SortUnit: (depth:2)org.jboss.errai.demo.client.presenter.ContactsPresenter => [ (depth:0)org.jboss.errai.demo.client.view.ContactsView => [],  (depth:0)org.jboss.errai.ioc.client.api.Caller => [],  (depth:1)com.google.gwt.event.shared.HandlerManager => [ (depth:0)org.jboss.errai.demo.client.Contacts => []],  (depth:0)org.jboss.errai.demo.client.presenter.ContactsPresenter$Display => [],  (depth:0)org.jboss.errai.ioc.client.api.builtin.CallerProvider$1 => []]
   [INFO] ProcessingDelegate:org.jboss.errai.demo.client.presenter.ContactsPresenter
   [INFO] SortUnit: (depth:3)org.jboss.errai.demo.client.Contacts => [ (depth:1)org.jboss.errai.demo.client.AppController => [ (depth:0)com.google.gwt.event.shared.HandlerManager => [],  (depth:0)org.jboss.errai.ioc.client.container.IOCBeanManager => []]]
   [INFO] ProcessingDelegate:org.jboss.errai.demo.client.Contacts
   [INFO] SortUnit: (depth:2)com.google.gwt.event.shared.HandlerManager => [ (depth:1)org.jboss.errai.demo.client.Contacts => [ (depth:0)org.jboss.errai.demo.client.AppController => []]]
   [INFO] ProcessingDelegate:org.jboss.errai.demo.client.Contacts
   [INFO] SortUnit: (depth:2)org.jboss.errai.demo.client.AppController => [ (depth:1)com.google.gwt.event.shared.HandlerManager => [ (depth:0)org.jboss.errai.demo.client.Contacts => []],  (depth:0)org.jboss.errai.ioc.client.container.IOCBeanManager => []]
   [INFO] ProcessingDelegate:org.jboss.errai.demo.client.AppController
   */

  final SortUnit messageBusProvider = sortUnitOf(MessageBusProvider.class);
  final SortUnit eventProvider = sortUnitOf(EventProvider.class);
  final SortUnit rootPanelProvider = sortUnitOf(RootPanelProvider.class);
  final SortUnit requestDispatcherProvider = sortUnitOf(RequestDispatcherProvider.class);
  final SortUnit callerProvider = sortUnitOf(CallerProvider.class);
  final SortUnit iocBeanManagerProvider = sortUnitOf(IOCBeanManagerProvider.class);
  final SortUnit instanceProvider = sortUnitOf(InstanceProvider.class);
  final SortUnit editContactPresenter = sortUnitOf(EditContactPresenter.class, sortUnitOf(Caller.class), sortUnitOf(EditContactView.class), sortUnitOf(EditContactPresenter.Display.class), sortUnitOf(HandlerManager.class), sortUnitOf(CallerProvider.class));
  final SortUnit contactsPresenter = sortUnitOf(ContactsPresenter.class, sortUnitOf(ContactsView.class), sortUnitOf(Caller.class), sortUnitOf(HandlerManager.class), sortUnitOf(ContactsPresenter.Display.class), sortUnitOf(CallerProvider.class));


  private static boolean isInRange(int start, int end, SortUnit su, List<SortUnit> list) {
    for (int i = start; i < list.size() && i < end; i++) {
      if (list.get(i).equals(su)) return true;
    }
    return false;
  }

  private static SortUnit hardDep(Class ref) {
    return new SortUnit(get(ref), true);
  }

  private static SortUnit sortUnitOf(Class ref, SortUnit... deps) {
    return new SortUnit(get(ref), null, new HashSet<SortUnit>(asList(deps)));
  }

}
