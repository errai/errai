/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.ResourceProvider;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.ModelAdapter;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.framework.RoutingFlags;

import java.util.Map;

public class MessageModelWrapper implements Message {
    private Message delegate;
    private transient final ModelAdapter modelAdapter;

    public MessageModelWrapper(Message delegate, ModelAdapter modelAdapter) {
        this.delegate = delegate;
        if ((this.modelAdapter = modelAdapter) == null) {
            throw new AssertionError("ModelAdapter cannot be null!");
        }
    }

    public Message toSubject(String subject) {
        delegate.toSubject(subject);
        return this;
    }

    public String getSubject() {
        return delegate.getSubject();
    }

    public Message command(String type) {
        delegate.command(type);
        return this;
    }

    public Message command(Enum type) {
        delegate.command(type);
        return this;
    }

    public String getCommandType() {
        return delegate.getCommandType();
    }

    public Message set(String part, Object value) {
        delegate.set(part, modelAdapter.clone(value));
        return this;
    }

    public Message set(Enum part, Object value) {
        delegate.set(part, modelAdapter.clone(value));
        return this;
    }

    public Message setProvidedPart(String part, ResourceProvider provider) {
        delegate.set(part, provider);
        return this;
    }

    public Message setProvidedPart(Enum part, ResourceProvider provider) {
        delegate.setProvidedPart(part, provider);
        return this;
    }

    public boolean hasPart(String part) {
        return delegate.hasPart(part);
    }

    public boolean hasPart(Enum part) {
        return delegate.hasPart(part);
    }

    public void remove(String part) {
        delegate.remove(part);
    }

    public void remove(Enum part) {
        delegate.remove(part);
    }

    public Message copy(String part, Message m) {
        delegate.copy(part, m);
        return this;
    }

    public Message copy(Enum part, Message m) {
        delegate.copy(part, m);
        return this;
    }

    public Message setParts(Map<String, Object> parts) {
        delegate.setParts(parts);
        return this;
    }

    public Message addAllParts(Map<String, Object> parts) {
        delegate.addAllParts(parts);
        return this;
    }

    public Message addAllProvidedParts(Map<String, ResourceProvider> provided) {
        delegate.addAllProvidedParts(provided);
        return this;
    }

    public Map<String, Object> getParts() {
        return delegate.getParts();
    }

    public Map<String, ResourceProvider> getProvidedParts() {
        return delegate.getProvidedParts();
    }

    public void addResources(Map<String, ?> resources) {
        delegate.addResources(resources);
    }

    public Message setResource(String key, Object res) {
        delegate.setResource(key, res);
        return this;
    }

    public <T> T getResource(Class<T> type, String key) {
        return delegate.getResource(type, key);
    }

    public boolean hasResource(String key) {
        return delegate.hasResource(key);
    }

    public Message copyResource(String key, Message m) {
        delegate.copyResource(key, m);
        return this;
    }

    public Message errorsCall(ErrorCallback callback) {
        delegate.errorsCall(callback);
        return this;
    }

    public ErrorCallback getErrorCallback() {
        return delegate.getErrorCallback();
    }

    public <T> T get(Class<T> type, String part) {
        return (T) modelAdapter.merge(delegate.get(type, part));
    }

    public <T> T get(Class<T> type, Enum part) {
        return (T) modelAdapter.merge(delegate.get(type, part));
    }

    public void setFlag(RoutingFlags flag) {
        delegate.setFlag(flag);
    }

    public void unsetFlag(RoutingFlags flag) {
        delegate.unsetFlag(flag);
    }

    public boolean isFlagSet(RoutingFlags flag) {
        return delegate.isFlagSet(flag);
    }

    public void commit() {
        delegate.commit();
    }

    public boolean isCommited() {
        return delegate.isCommited();
    }

    public void sendNowWith(MessageBus viaThis) {
        delegate.sendNowWith(viaThis);
    }

    public void sendNowWith(RequestDispatcher viaThis) {
        delegate.sendNowWith(viaThis);
    }
}
