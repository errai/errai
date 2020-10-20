/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.websocket.test.jsr356.cdi.adapter;

import java.io.File;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.Conversation;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.errai.bus.server.websocket.jsr356.weld.conversation.ConversationState;
import org.jboss.errai.bus.server.websocket.jsr356.weld.conversation.WeldConversationScopeAdapter;
import org.jboss.errai.bus.server.websocket.jsr356.weld.request.WeldRequestScopeAdapter;
import org.jboss.errai.bus.server.websocket.jsr356.weld.session.WeldSessionScopeAdapter;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.manager.BeanManagerImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the contexts and scopes.
 *
 * @author Michel Werren
 */
@Ignore //FIXME: Remove this @Ignore and fix issues with arquillian/WildFly14
@RunWith(Arquillian.class)
public class ContainerOnlyTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContainerOnlyTest.class.getName());

  @SuppressWarnings("rawtypes")
  @Deployment
  public static Archive getDeployment() {
    final WebArchive war = ShrinkWrap.create(WebArchive.class, "containertest.war");
    war.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    war.addClasses(RequestScopedBean.class, SessionScopedBean.class, FakeHttpSession.class, ConversationScopeBean.class);
    war.addPackages(true, "org.jboss.errai.bus.server.websocket.jsr356");
    final File[] files = Maven.resolver().loadPomFromFile("./pom.xml", "test-dependency-override")
            .resolve("org.jboss.errai:errai-bus:?", "com.google.guava:guava:?")
            .withTransitivity()
            .asFile();
    for (final File file : files) {
      war.addAsLibrary(file);
    }
    return war;
  }

  @Inject
  private BoundRequestContext boundRequestContext;

  @Inject
  private BoundConversationContext boundConversationContext;

  @Inject
  private BeanManagerImpl beanManager;

  @Inject
  private Conversation conversation;

  @Before
  public void setUp() throws Exception {
    WeldRequestScopeAdapter.init(boundRequestContext);
    WeldSessionScopeAdapter.init(beanManager);
    WeldConversationScopeAdapter.init(boundConversationContext);
  }

  /**
   * Test for 6 concurrent {@link javax.enterprise.context.RequestScoped} beans.
   * Each one has its own {@link Thread}.
   *
   * @throws Exception
   */
  @Test
  public void testRequestScope() throws Exception {

    final AtomicInteger doneRunnerCount = new AtomicInteger(0);

    final Runnable requestScopeTask = new Runnable() {
      @Override
      public void run() {
        final long timestamp = System.currentTimeMillis();
        WeldRequestScopeAdapter.getInstance().activateContext();
        final RequestScopedBean requestScopedBean = getInstance(RequestScopedBean.class);
        requestScopedBean.setTimestamp(timestamp);
        try {
          Thread.sleep(500);
        } catch (final InterruptedException e) {
          Assert.fail();
        }
        Assert.assertEquals(timestamp, requestScopedBean.getTimestamp());
        WeldRequestScopeAdapter.getInstance().invalidateContext();
        doneRunnerCount.incrementAndGet();
      }
    };

    final Thread one = new Thread(requestScopeTask);
    final Thread two = new Thread(requestScopeTask);
    final Thread three = new Thread(requestScopeTask);
    final Thread four = new Thread(requestScopeTask);
    final Thread five = new Thread(requestScopeTask);
    final Thread six = new Thread(requestScopeTask);

    one.start();
    two.start();
    three.start();
    four.start();
    five.start();
    six.start();

    /* Avoid hanging */
    final long startTime = System.currentTimeMillis();
    while (doneRunnerCount.get() < 6 && System.currentTimeMillis() - startTime < 2000) {
      Thread.sleep(10);
    }

    if (doneRunnerCount.get() < 6) {
      LOGGER.error("not all runners done.");
      Assert.fail();
    }
  }

  /**
   * Test 2 concurrent sessions in each 3 {@link Thread}. That's each
   * {@link Thread} obtain the correct
   * {@link javax.enterprise.context.SessionScoped} bean and its value.
   *
   * @throws Exception
   */
  @Test
  public void testSessionScope() throws Exception {
    final AtomicInteger doneRunnerCount = new AtomicInteger(0);

    final Short idFirstSession = 1;
    final Short idSecondSession = 2;

    final FakeHttpSession firstSession = new FakeHttpSession();
    final FakeHttpSession secondSession = new FakeHttpSession();

    final AtomicBoolean done = new AtomicBoolean(false);

    final Runnable firstSessionActivator = new Runnable() {
      @Override
      public void run() {
        WeldSessionScopeAdapter.getInstance().activateContext(firstSession);
        final SessionScopedBean sessionScopedBean = getInstance(SessionScopedBean.class);
        sessionScopedBean.setId(idFirstSession);
        WeldSessionScopeAdapter.getInstance().deactivateContext();
        done.set(true);
      }
    };

    new Thread(firstSessionActivator).start();

    while (!done.get()) {
      Thread.sleep(10);
    }

    done.set(false);

    final Runnable secondSessionActivator = new Runnable() {
      @Override
      public void run() {
        WeldSessionScopeAdapter.getInstance().activateContext(secondSession);
        final SessionScopedBean sessionScopedBean = getInstance(SessionScopedBean.class);
        sessionScopedBean.setId(idSecondSession);
        WeldSessionScopeAdapter.getInstance().deactivateContext();
        done.set(true);
      }
    };

    new Thread(secondSessionActivator).start();

    while (!done.get()) {
      Thread.sleep(10);
    }

    class SessionRunner implements Runnable {

      private final HttpSession httpSession;

      private final Short id;

      SessionRunner(final HttpSession httpSession, final Short id) {
        this.httpSession = httpSession;
        this.id = id;
      }

      @Override
      public void run() {
        WeldSessionScopeAdapter.getInstance().activateContext(httpSession);
        final SessionScopedBean sessionScopedBean = getInstance(SessionScopedBean.class);
        Assert.assertEquals("not same session in different thread", id, sessionScopedBean.getId());
        WeldSessionScopeAdapter.getInstance().deactivateContext();
        doneRunnerCount.incrementAndGet();
      }
    }

    final Thread one = new Thread(new SessionRunner(firstSession, idFirstSession));
    final Thread two = new Thread(new SessionRunner(secondSession, idSecondSession));
    final Thread three = new Thread(new SessionRunner(firstSession, idFirstSession));
    final Thread four = new Thread(new SessionRunner(secondSession, idSecondSession));
    final Thread five = new Thread(new SessionRunner(firstSession, idFirstSession));
    final Thread six = new Thread(new SessionRunner(secondSession, idSecondSession));

    one.start();
    two.start();
    three.start();
    four.start();
    five.start();
    six.start();

    /* Avoid hanging */
    final long startTime = System.currentTimeMillis();
    while (doneRunnerCount.get() < 6 && System.currentTimeMillis() - startTime < 2000) {
      Thread.sleep(10);
    }

    if (doneRunnerCount.get() < 6) {
      LOGGER.error("not all runners done.");
      Assert.fail();
    }
  }

  /**
   * Test for 2 concurrent active {@link Conversation} in one
   * {@link HttpSession} {@link ConversationScopeBean} must anytime have the
   * same id as the {@link ConversationState}. Runs with 4 concurrent
   * {@link Thread} per {@link Conversation}.
   *
   * @throws Exception
   */
  @Test
  public void testConversationScope() throws Exception {
    final FakeHttpSession httpSession = new FakeHttpSession();
    final ConversationState firstCS = new ConversationState();
    final ConversationState secondCS = new ConversationState();
    final AtomicInteger doneRunnerCount = new AtomicInteger(0);

    final AtomicBoolean done = new AtomicBoolean(false);

    final Runnable firstConversationActivator = new Runnable() {
      @Override
      public void run() {
        /* Like begin of message processing */
        WeldRequestScopeAdapter.getInstance().activateContext();
        WeldSessionScopeAdapter.getInstance().activateContext(httpSession);
        WeldConversationScopeAdapter.getInstance().activateContext(firstCS);

        conversation.begin();
        final ConversationScopeBean conversationScopeBean = getInstance(ConversationScopeBean.class);
        conversationScopeBean.setId(conversation.getId());

        WeldConversationScopeAdapter.getInstance().deactivateContext();
        WeldSessionScopeAdapter.getInstance().deactivateContext();
        WeldRequestScopeAdapter.getInstance().deactivateContext();

        done.set(true);
      }
    };

    new Thread(firstConversationActivator).start();

    /* wait until conversation has begun */
    while (!done.get()) {
      Thread.sleep(10);
    }

    done.set(false);

    final Runnable secondConversationActivator = new Runnable() {
      @Override
      public void run() {
        /* Like begin of message processing */
        WeldRequestScopeAdapter.getInstance().activateContext();
        WeldSessionScopeAdapter.getInstance().activateContext(httpSession);
        WeldConversationScopeAdapter.getInstance().activateContext(secondCS);

        conversation.begin();
        final ConversationScopeBean conversationScopeBean = getInstance(ConversationScopeBean.class);
        conversationScopeBean.setId(conversation.getId());

        WeldConversationScopeAdapter.getInstance().deactivateContext();
        WeldSessionScopeAdapter.getInstance().deactivateContext();
        WeldRequestScopeAdapter.getInstance().deactivateContext();

        WeldRequestScopeAdapter.getInstance().activateContext();
        WeldSessionScopeAdapter.getInstance().activateContext(httpSession);
        WeldConversationScopeAdapter.getInstance().activateContext(secondCS);

        WeldConversationScopeAdapter.getInstance().deactivateContext();
        WeldSessionScopeAdapter.getInstance().deactivateContext();
        WeldRequestScopeAdapter.getInstance().deactivateContext();

        done.set(true);
      }
    };

    new Thread(secondConversationActivator).start();

    /* wait until conversation has begun */
    while (!done.get()) {
      Thread.sleep(10);
    }

    class ConversationRunner implements Runnable {

      private final ConversationState conversationState;

      private final Conversation conversation;

      ConversationRunner(final ConversationState conversationState, final Conversation conversation) {
        this.conversationState = conversationState;
        this.conversation = conversation;
      }

      @Override
      public void run() {
        WeldRequestScopeAdapter.getInstance().activateContext();
        WeldSessionScopeAdapter.getInstance().activateContext(httpSession);
        WeldConversationScopeAdapter.getInstance().activateContext(conversationState);

        Assert.assertFalse(conversation.isTransient());
        Assert.assertTrue(conversationState.isLongRunning());

        final ConversationScopeBean conversationScopeBean = getInstance(ConversationScopeBean.class);
        Assert.assertEquals("wrong state", conversationState.getConversationId(), conversationScopeBean.getId());

        WeldConversationScopeAdapter.getInstance().deactivateContext();
        WeldSessionScopeAdapter.getInstance().deactivateContext();
        WeldRequestScopeAdapter.getInstance().deactivateContext();

        final int runner = doneRunnerCount.incrementAndGet();
        LOGGER.info("conversation {} runner: {} done", conversationState.getConversationId(), runner);
      }
    }

    LOGGER.info("first conversation id: {}", firstCS.getConversationId());
    LOGGER.info("second conversation id: {}", secondCS.getConversationId());

    final Thread firstFirst = new Thread(new ConversationRunner(firstCS, conversation));
    final Thread firstSecond = new Thread(new ConversationRunner(firstCS, conversation));
    final Thread firstThird = new Thread(new ConversationRunner(firstCS, conversation));
    final Thread firstFourth = new Thread(new ConversationRunner(firstCS, conversation));

    final Thread secondFirst = new Thread(new ConversationRunner(secondCS, conversation));
    final Thread secondSecond = new Thread(new ConversationRunner(secondCS, conversation));
    final Thread secondThird = new Thread(new ConversationRunner(secondCS, conversation));
    final Thread secondFourth = new Thread(new ConversationRunner(secondCS, conversation));

    firstFirst.start();
    secondFirst.start();
    firstSecond.start();
    secondSecond.start();
    firstThird.start();
    secondThird.start();
    firstFourth.start();
    secondFourth.start();

    /* Avoid hanging */
    final long startTime = System.currentTimeMillis();
    while (doneRunnerCount.get() < 8 && System.currentTimeMillis() - startTime < 2000) {
      Thread.sleep(10);
    }

    if (doneRunnerCount.get() < 8) {
      LOGGER.error("not all runners done.");
      Assert.fail();
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getInstance(final Class<T> clazz) {
    final Set<Bean<?>> beans = beanManager.getBeans(clazz);
    final Bean<?> bean = beans.iterator().next();
    final Object reference = beanManager.getReference(bean, bean.getBeanClass(),
            beanManager.createCreationalContext(bean));
    return (T) reference;
  }
}
