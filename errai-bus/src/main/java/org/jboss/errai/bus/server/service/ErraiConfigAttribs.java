/*
 * Copyright 2012 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server.service;

/**
 * @author Mike Brock
 */
public enum ErraiConfigAttribs {

  /**
   * The buffer size in megabytes. If this attribute is specified along with {@link #BUS_BUFFER_SEGMENT_SIZE}
   * then the segment count is inferred by the simple calculation {@code BUF_BUFFER_SIZE / BUS_BUFFER_SEGMENT_SIZE}.
   * If the {@link #BUS_BUFFER_SEGMENT_COUNT} is specified, it will be ignored in the presence of this attribute.
   * <p/>
   * Default value: 128
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
   * Default value: 'heap'
   */
  BUS_BUFFER_ALLOCATION_MODE("errai.bus.buffer_allocation_mode", "heap"),


  WEBSOCKET_SERVLET_ENABLED("org.jboss.errai.websocket.servlet.enabled", "false"),
  WEBSOCKET_SERVLET_CONTEXT_PATH("org.jboss.errai.websocket.servlet.path", "in.erraiBusWebSocket");

  protected final String attributeName;
  protected final String defaultValue;


  private ErraiConfigAttribs(String attributeName) {
    this(attributeName, null);
  }

  ErraiConfigAttribs(String attributeName, String defaultValue) {
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

  private void setDefaultValue(ErraiServiceConfigurator configurator) {
    if (defaultValue != null && !configurator.hasProperty(getAttributeName())) {
      configurator.setProperty(getAttributeName(), defaultValue);
    }
  }

  public String getAttributeName() {
    return attributeName;
  }
}
