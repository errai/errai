/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
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


package org.jboss.errai.bus.server.servlet;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CSRFTokenFilterTest {

    final static String chinese_loadingPleaseWait = "请稍候";
    final static String chinese_loadingApplication = "正在加载应用程序......";

    final static String japanese_loadingPleaseWait = "しばらくお待ちください";
    final static String japanese_loadingApplication = "アプリケーションをロード中 ...";

    @Mock
    HttpServletRequest req ;

    @Mock
    HttpServletResponse resp;

    @Mock
    FilterChain chain ;

    @Mock
    HttpSession session;

    final CharArrayWriter charWriter = new CharArrayWriter();

    @Before
    public void init() throws IOException{
        when(req.getSession(false)).thenReturn(session);
        when(req.getMethod()).thenReturn("GET");
        when(resp.getWriter()).thenReturn(new PrintWriter(charWriter));
        when(resp.getContentType()).thenReturn("text/html");
    }

    @Test
    public void testChinese() throws IOException, ServletException {
        csfrTokenFilterTest("/test-html/sample-Chinese.html", chinese_loadingPleaseWait, chinese_loadingApplication);
    }

    @Test
    public void testJapanese() throws IOException, ServletException {
        csfrTokenFilterTest("/test-html/sample-Japanese.html", japanese_loadingPleaseWait, japanese_loadingApplication);
    }

    private void csfrTokenFilterTest(String path, String loadingPleaseWait, String loadingApplication)throws IOException, ServletException{
        CSRFTokenFilter csrfTokenFilter = new CSRFTokenFilter();

        doAnswer(ctx -> {
            final InputStream stream = getClass().getResourceAsStream(path);
            final String text = IOUtils.toString(stream);
            ((HttpServletResponse) ctx.getArguments()[1]).getWriter().print(text);
            return null;
        }).when(chain).doFilter(any(),any());

        csrfTokenFilter.doFilter(req, resp, chain);

        final Document document = Jsoup.parse(charWriter.toString());
        Elements es = document.select("div.center-block");

        assertEquals(loadingPleaseWait, es.get(1).text());
        assertEquals(loadingApplication, es.get(2).text());
    }
}
