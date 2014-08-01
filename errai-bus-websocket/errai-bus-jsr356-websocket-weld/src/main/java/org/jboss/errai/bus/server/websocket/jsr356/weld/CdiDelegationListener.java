package org.jboss.errai.bus.server.websocket.jsr356.weld;

import org.jboss.errai.bus.server.websocket.jsr356.channel.ErraiChannelFactory;
import org.jboss.errai.bus.server.websocket.jsr356.filter.FilterLookup;
import org.jboss.errai.bus.server.websocket.jsr356.weld.channel.CdiErraiChannelFactory;
import org.jboss.errai.bus.server.websocket.jsr356.weld.conversation.WeldConversationScopeAdapter;
import org.jboss.errai.bus.server.websocket.jsr356.weld.filter.CdiFilterLookup;
import org.jboss.errai.bus.server.websocket.jsr356.weld.request.WeldRequestScopeAdapter;
import org.jboss.errai.bus.server.websocket.jsr356.weld.session.WeldSessionScopeAdapter;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.context.bound.BoundSessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * @author Michel Werren
 */
@WebListener
public class CdiDelegationListener implements ServletContextListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(CdiDelegationListener.class.getName());

  @Inject
  private BoundSessionContext boundSessionContext;

  @Inject
  private BoundRequestContext boundRequestContext;

  @Inject
  private BoundConversationContext boundConversationContext;

  @Inject
  private CdiFilterLookup cdiFilterLookup;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    WeldConversationScopeAdapter.init(boundConversationContext);
    WeldSessionScopeAdapter.init(boundSessionContext);
    WeldRequestScopeAdapter.init(boundRequestContext);

    ErraiChannelFactory.registerDelegate(CdiErraiChannelFactory.getInstance());
    FilterLookup.registerDelegate(cdiFilterLookup);

    LOGGER.info("CDI activated for Errai Bus JSR-356 websocket");
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {

  }
}
