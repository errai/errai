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
import javax.enterprise.context.ApplicationScoped;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jboss.errai.bus.server.servlet.CSRFTokenCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.errai.common.client.framework.Constants.*;

@ApplicationScoped
public class CSRTokenHandler implements InjectScriptHandler {

    private static Logger log = LoggerFactory.getLogger(CSRTokenHandler.class);

    @Override
    public List<String> injectScript(List<String> injectedScriptList, ServletRequest request, ServletResponse response) throws IOException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        ensureSessionHasToken(httpRequest.getSession(false));

        switch (httpRequest.getMethod().toUpperCase()) {
            case "POST":
            case "PUT":
            case "DELETE": {
                if (CSRFTokenCheck.INSTANCE.isInsecure(httpRequest, log)) {
                    CSRFTokenCheck.INSTANCE.prepareResponse(httpRequest, (HttpServletResponse) response, log);
                    return injectedScriptList;
                }
            }
            case "GET": {
                final HttpSession session = httpRequest.getSession(false);
                final String responseContentType = response.getContentType();
                if (session != null && responseContentType != null && responseContentType.toLowerCase().startsWith("text/html")) {
                    CSRFTokenCheck.INSTANCE.prepareSession(session, log);
                    injectedScriptList.add("<script>var " + ERRAI_CSRF_TOKEN_VAR + " = '" + CSRFTokenCheck.getToken(session) + "';</script>");
                }
            }
        }
        return injectedScriptList;
    }

    private void ensureSessionHasToken(final HttpSession session) {
        if (session != null) {
            CSRFTokenCheck.INSTANCE.prepareSession(session, log);
        }
    }
}
