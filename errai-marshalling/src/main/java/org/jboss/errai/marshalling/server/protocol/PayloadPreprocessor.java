package org.jboss.errai.marshalling.server.protocol;

import java.util.Map;

public interface PayloadPreprocessor {

  void process(Map<String, Object> payload);
}
