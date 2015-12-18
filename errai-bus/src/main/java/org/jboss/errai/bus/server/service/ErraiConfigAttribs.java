/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.service;

import org.jboss.errai.bus.server.HttpSessionProvider;
import org.jboss.errai.bus.server.SimpleDispatcher;

/**
 * @author Mike Brock
 */
public enum ErraiConfigAttribs {

  ERRAI_DISPATCHER_IMPLEMENTATION("errai.dispatcher_implementation", SimpleDispatcher.class.getName()),
  ERRAI_SESSION_PROVIDER_IMPLEMENTATION("errai.session_provider_implementation", HttpSessionProvider.class.getName()),

  /**
   * The buffer size in megabytes. If this attribute is specified along with {@link #BUS_BUFFER_SEGMENT_SIZE}
   * then the segment count is inferred by the simple calculation {@code BUF_BUFFER_SIZE / BUS_BUFFER_SEGMENT_SIZE}.
   * If the {@link #BUS_BUFFER_SEGMENT_COUNT} is specified, it will be ignored in the presence of this attribute.
   * <p/>
   * Default value: 32
   */
  BUS_BUFFER_SIZE("errai.bus.buffer_size"),

  /**
   * The segment size in bytes.
   * <p/>
   * Defualt value: 8
   */
  BUS_BUFFER_SEGMENT_SIZE("errai.bus.buffer_segment_size"),

  /**
   * The number of segments in absolute terms. If this attribute is specified in the absense of {@link #BUS_BUFFER_SIZE}
   * then the buffer size is inferred by the calculation {@code BUS_BUFFER_SEGMENT_SIZE * BUS_BUFFER_SEGMENT_COUNT}
   * <p/>
   */
  BUS_BUFFER_SEGMENT_COUNT("errai.bus.buffer_segment_count"),


  /**
   * Allocation mode ('direct' or 'heap'). Direct allocation will allocate memory outside
   * of the JVM heap, while heap allocation will be allocated inside the Java heap. For most situations, heap
   * allocation is preferable. However, if the application is data intensive and requires a substantially large
   * buffer, it is preferable to use a direct buffer. From a throughput perspective, you can expect to pay
   * about a 20% performance penalty. However, your application may show better scaling characteristics with
   * direct buffers. Benchmarking may be necessary to properly tune this setting for your use case and expected
   * load.
   * <p/>
   * Default value: 'direct'
   */
  BUS_BUFFER_ALLOCATION_MODE("errai.bus.buffer_allocation_mode", "direct"),

  HOSTED_MODE_TESTING("errai.hosted_mode_testing", "false"),
  DO_LONG_POLL("org.jboss.errai.bus.do_long_poll", "true"),
  LONG_POLL_TIMEOUT("errai.bus.long_poll_timeout", "45000"),

  ENABLE_SSE_SUPPORT("errai.bus.enable_sse_support", "true"),
  SSE_TIMEOUT("errai.bus.servlet_sse_timeout", "45000"),

  ENABLE_WEB_SOCKET_SERVER("errai.bus.enable_web_socket_server", "false"),
  WEB_SOCKET_URL("errai.bus.web_socket_url", "/websocket.bus"),
  WEB_SOCKET_PORT("errai.bus.web_socket_port", "8085"),
  SECURE_WEB_SOCKET_SERVER("errai.bus.secure_web_socket_server", "false"),
  WEB_SOCKET_KEYSTORE("errai.bus.web_socket_keystore"),
  WEB_SOCKET_KEYSTORE_TYPE("errai.bus.web_socket_keystore_type", "JKS"),
  WEB_SOCKET_KEYSTORE_PASSWORD("errai.bus.web_socket_keystore_password"),
  WEB_SOCKET_KEY_PASSWORD("errai.bus.web_socket_key_password"),

  WEBSOCKET_SERVLET_ENABLED("errai.bus.websocket.servlet.enabled", "false"),
  WEBSOCKET_SERVLET_CONTEXT_PATH("errai.bus.websocket.servlet.path", "in.erraiBusWebSocket"),
  FORCE_SECURE_WEBSOCKET("errai.bus.websocket.force.secure", "false"),

  AUTO_DISCOVER_SERVICES("errai.bus.auto_discover_services", "false"),

  CLUSTER_PORT("errai.clustering.port", "6446"),
  CLUSTER_NAME("errai.bus.cluster_name", "errai"),
  ENABLE_CLUSTERING("errai.bus.enable_clustering", "false"),
  CLUSTERING_PROVIDER("errai.bus.clustering_provider", "org.jboss.errai.bus.server.cluster.noop.NoopClusteringProvider"),

  MESSAGE_QUEUE_TIMEOUT_SECS("errai.bus.message_queue_timeout_secs", "90"),
  SATURATION_POLICY("errai.bus.saturation_policy", "CallerRuns");

  protected final String attributeName;
  protected final String defaultValue;

  private ErraiConfigAttribs(final String attributeName) {
    this(attributeName, null);
  }

  ErraiConfigAttribs(final String attributeName, final String defaultValue) {
    this.attributeName = attributeName;
    this.defaultValue = defaultValue;
  }

  public boolean getBoolean(final ErraiServiceConfigurator configurator) {
    setDefaultValue(configurator);
    return configurator.getBooleanProperty(getAttributeName());
  }

  public Integer getInt(final ErraiServiceConfigurator configurator) {
    setDefaultValue(configurator);
    return configurator.getIntProperty(getAttributeName());
  }

  public String get(final ErraiServiceConfigurator configurator) {
    setDefaultValue(configurator);
    return configurator.getProperty(getAttributeName());
  }

  public void set(final ErraiServiceConfigurator configurator, final String value) {
    configurator.setProperty(getAttributeName(), value);
  }

  private void setDefaultValue(final ErraiServiceConfigurator configurator) {
    if (defaultValue != null && !configurator.hasProperty(getAttributeName())) {
      configurator.setProperty(getAttributeName(), defaultValue);
    }
  }

  public String getAttributeName() {
    return attributeName;
  }
}
