package org.jboss.errai.cdi.server.events;

import java.util.Map;

import org.jboss.errai.bus.client.api.RoutingFlag;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.ResourceProvider;

public class MockMessage implements Message {

  @Override
  public Message toSubject(String subject) {
    return null;
  }

  @Override
  public String getSubject() {
    return null;
  }

  @Override
  public Message command(String type) {
    return null;
  }

  @Override
  public Message command(Enum<?> type) {
    return null;
  }

  @Override
  public String getCommandType() {
    return null;
  }

  @Override
  public Message set(String part, Object value) {
    return null;
  }

  @Override
  public Message set(Enum<?> part, Object value) {
    return null;
  }

  @Override
  public Message setProvidedPart(String part, ResourceProvider<?> provider) {
    return null;
  }

  @Override
  public Message setProvidedPart(Enum<?> part, ResourceProvider<?> provider) {
    return null;
  }

  @Override
  public boolean hasPart(String part) {
    return false;
  }

  @Override
  public boolean hasPart(Enum<?> part) {
    return false;
  }

  @Override
  public void remove(String part) {
  }

  @Override
  public void remove(Enum<?> part) {
  }

  @Override
  public Message copy(String part, Message m) {
    return null;
  }

  @Override
  public Message copy(Enum<?> part, Message m) {
    return null;
  }

  @Override
  public Message setParts(Map<String, Object> parts) {
    return null;
  }

  @Override
  public Message addAllParts(Map<String, Object> parts) {
    return null;
  }

  @Override
  public Message addAllProvidedParts(Map<String, ResourceProvider<?>> provided) {
    return null;
  }

  @Override
  public Map<String, Object> getParts() {
    return null;
  }

  @Override
  public Map<String, ResourceProvider<?>> getProvidedParts() {
    return null;
  }

  @Override
  public void addResources(Map<String, ?> resources) {
  }

  @Override
  public Message setResource(String key, Object res) {
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getResource(Class<T> type, String key) {
    return (T) new MockQueueSession();
  }

  @Override
  public boolean hasResource(String key) {
    return false;
  }

  @Override
  public Message copyResource(String key, Message m) {
    return null;
  }

  @Override
  public Message errorsCall(@SuppressWarnings("rawtypes") ErrorCallback callback) {
    return null;
  }

  @Override
  public ErrorCallback<Message> getErrorCallback() {
    return null;
  }

  @Override
  public <T> T getValue(Class<T> type) {
    return null;
  }

  @Override
  public <T> T get(Class<T> type, String part) {
    return null;
  }

  @Override
  public <T> T get(Class<T> type, Enum<?> part) {
    return null;
  }

  @Override
  public Message setFlag(RoutingFlag flag) {
    return null;
  }

  @Override
  public void unsetFlag(RoutingFlag flag) {
  }

  @Override
  public boolean isFlagSet(RoutingFlag flag) {
    return false;
  }

  @Override
  public void commit() {
  }

  @Override
  public boolean isCommited() {
    return false;
  }

  @Override
  public void sendNowWith(MessageBus viaThis) {
  }

  @Override
  public void sendNowWith(RequestDispatcher viaThis) {
  }

}
