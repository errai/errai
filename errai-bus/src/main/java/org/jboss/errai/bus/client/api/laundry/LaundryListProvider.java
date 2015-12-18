/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.api.laundry;

/**
 * The means of obtaining a {@link LaundryList} instance either on the client or the server. LaundryListProviders should
 * be obtained from the {@link LaundryListProviderFactory}.
 * 
 * @author Mike Brock
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface LaundryListProvider {
  
  /**
   * Returns the laundry list for the given object, which must be an Errai Bus session.
   * 
   * @param ref The Errai Bus session.
   * @return The {@link LaundryList} for the given session.
   */
  public LaundryList getLaundryList(Object ref);
}
