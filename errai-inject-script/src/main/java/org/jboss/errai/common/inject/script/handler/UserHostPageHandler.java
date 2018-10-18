/*
 *
 *  * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package org.jboss.errai.common.inject.script.handler;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.marshalling.server.ServerMarshalling;
import org.jboss.errai.security.server.properties.ErraiAppProperties;
import org.jboss.errai.security.shared.api.SecurityConstants;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;

import static org.jboss.errai.security.Properties.*;

@ApplicationScoped
public class UserHostPageHandler implements InjectScriptHandler {

    @Inject
    private AuthenticationService authenticationService;

    @Inject
    @ErraiAppProperties
    private Properties properties;

    @PostConstruct
    public void init(){
        // Initialize server side marshaller
        MappingContextSingleton.get();
    }

    @Override
    public List<String> injectScript(List<String> injectedScriptList, ServletRequest request, ServletResponse response) throws IOException {
        if (isUserOnHostPageEnabled()) {
            final User user = authenticationService.getUser();

            if (user != null) {
                final String injectedScript = "<script>var " +
                        SecurityConstants.ERRAI_SECURITY_CONTEXT_DICTIONARY + "  = " +
                        securityContextJson(user) + "; </script>";

                injectedScriptList.add(injectedScript);
            }
        }
        return injectedScriptList;
    }

    String securityContextJson(final User user) {
        final String userJson = ServerMarshalling.toJSON(user);

        return "{\"" + SecurityConstants.DICTIONARY_USER + "\": " + userJson + "}";
    }

    private boolean isUserOnHostPageEnabled() {
        if (properties.containsKey(USER_ON_HOSTPAGE_ENABLED)) {
            return Boolean.parseBoolean(properties.getProperty(USER_ON_HOSTPAGE_ENABLED));
        }
        return false;
    }
}
