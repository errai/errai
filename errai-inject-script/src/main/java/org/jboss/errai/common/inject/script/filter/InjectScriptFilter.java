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

package org.jboss.errai.common.inject.script.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.jboss.errai.common.inject.script.handler.InjectScriptHandler;
import org.jboss.errai.common.server.FilterCacheUtil.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.errai.common.server.FilterCacheUtil.*;

public class InjectScriptFilter implements Filter {

    private static Logger log = LoggerFactory.getLogger(InjectScriptFilter.class);

    @Inject
    Instance<InjectScriptHandler> instances;

    private String injectScriptHandlerName;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        injectScriptHandlerName = filterConfig.getInitParameter("inject-script-handlers");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final CharResponseWrapper wrappedResponse = new CharResponseWrapper((HttpServletResponse) response);

        chain.doFilter(request, noCache(wrappedResponse));

        final Document document = Jsoup.parse(wrappedResponse.toString());

        List<String> injectedScriptList = execute(request, response);

        injectedScriptList.forEach(script -> {
            document.head().append(script);
        });

        String output = document.html();
        byte[] bytes = output.getBytes("UTF-8");

        writeStream(response, output, bytes);
    }

    private void writeStream(ServletResponse response, String output, byte[] bytes) throws IOException{
        if (response instanceof CharResponseWrapper) {
            response.getWriter().print(output);
        }
        response.getOutputStream().write(bytes);
    }

    private List<String> execute(ServletRequest request, ServletResponse response){
        List<String> injectedScriptList = new ArrayList<String>();

        Iterator<InjectScriptHandler> iterator = instances.iterator();
        while(iterator.hasNext()){
            InjectScriptHandler injectScriptHandler = iterator.next();

            String className = injectScriptHandler.getClass().getName().substring(0,injectScriptHandler.getClass().getName().indexOf("$"));
            if (injectScriptHandlerName.contains(className)){
                try {
                    injectScriptHandler.injectScript(injectedScriptList, request, response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return injectedScriptList;
    }

    @Override
    public void destroy() {

    }
}
