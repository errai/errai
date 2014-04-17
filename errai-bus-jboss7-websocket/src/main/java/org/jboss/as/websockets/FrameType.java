package org.jboss.as.websockets;

/**
 * @author Mike Brock
 */
public enum FrameType {
  Continuation,
  Text,
  Binary,
  Ping,
  Pong,
  ConnectionClose,
  Unknown
}