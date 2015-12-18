package org.jboss.errai.bus.server.websocket.jsr356.configuration;

import org.apache.commons.lang3.StringUtils;
import org.jboss.errai.bus.server.websocket.jsr356.filter.FilterLookup;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Michel Werren
 */
public class ErraiEndpointConfigurator extends ServerEndpointConfig.Configurator {

  public static final String FILTER_PARAM_NAME = "errai-jsr-356-websocket-filter";

  private static Boolean filterLookuped = Boolean.FALSE;

  @Override
  public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
    super.modifyHandshake(sec, request, response);
    final Map<String, Object> userProperties = sec.getUserProperties();
    applyHttpSessionInformation(userProperties, request);
    // Doing here instead of javax.servlet.annotation.WebFilter because
    // there is no order of execution.
    applyWebsocketFilters(request);
  }

  /**
   * Applies the {@link javax.servlet.http.HttpSession} to the configuration.
   * 
   * @param userInformation
   * @param handshakeRequest
   */
  private void applyHttpSessionInformation(Map<String, Object> userInformation, HandshakeRequest handshakeRequest) {
    userInformation.put(HttpSession.class.getName(), handshakeRequest.getHttpSession());
  }

  private void applyWebsocketFilters(HandshakeRequest handshakeRequest) {
    if (!filterLookuped) {
      HttpSession httpSession = ((HttpSession) handshakeRequest.getHttpSession());
      final String filterNames = httpSession.getServletContext().getInitParameter(FILTER_PARAM_NAME);
      if (!StringUtils.isEmpty(filterNames)) {
        final StringTokenizer filterTokenizer = new StringTokenizer(filterNames, ",");
        FilterLookup.getInstance().initFilters(filterTokenizer);
      }
      filterLookuped = Boolean.TRUE;
    }
  }

}
