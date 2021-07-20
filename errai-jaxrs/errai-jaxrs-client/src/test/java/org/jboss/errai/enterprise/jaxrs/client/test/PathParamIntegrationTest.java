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

package org.jboss.errai.enterprise.jaxrs.client.test;

import java.util.Date;

import javax.ws.rs.core.PathSegment;

import org.jboss.errai.enterprise.client.jaxrs.api.PathSegmentImpl;
import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.PathParamTestService;
import org.junit.Test;

/**
 * Testing path parameters.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class PathParamIntegrationTest extends AbstractErraiJaxrsTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  @Test
  public void testGetWithPathParam() {
    call(PathParamTestService.class,
        new SimpleAssertionCallback<>("@GET with @PathParam failed", 1l)).getWithPathParam(1l);
  }

  @Test
  public void testGetWithPathParamRegex() {
    call(PathParamTestService.class,
        new SimpleAssertionCallback<>("@GET with @PathParam using regex failed", 2l)).getWithPathParamRegex(2l);
  }

  @Test
  public void testGetWithPathParamRegexAndCurlyBracesQuantifier() {
    call(PathParamTestService.class,
        new SimpleAssertionCallback<>("@GET with @PathParam using regex failed", 2l)).getWithPathParamRegexAndCurlyBracesQuantifier(2l);
  }

  @Test
  public void testGetWithEncodedPathParam() {
    final String pathWithSpecialChars = "?<>!@#$%^\\&*()-+;:''\\/.,";
    call(PathParamTestService.class, new SimpleAssertionCallback<>("@GET w/ encoded @PathParam failed",
        pathWithSpecialChars)).getWithStringPathParam(pathWithSpecialChars);
  }

  @Test
  public void testGetWithPathSegmentPathParam() {
    final PathSegment ps = new PathSegmentImpl("path;name=nameValue;author=authorValue;empty=");
    assertEquals("path", ps.getPath());
    assertEquals("nameValue", ps.getMatrixParameters().getFirst("name"));
    assertEquals("authorValue", ps.getMatrixParameters().getFirst("author"));
    assertEquals("", ps.getMatrixParameters().getFirst("empty"));
    assertNull(ps.getMatrixParameters().getFirst("path"));

    call(PathParamTestService.class,
        new SimpleAssertionCallback<>("@GET with @PathParam failed", "nameValue/authorValue"))
        .getWithPathSegmentPathParam(ps);
  }
  @Test
  public void testGetWithDatePathParam() {
    final Date expected = new Date();
    call(PathParamTestService.class,
        new SimpleAssertionCallback<>("@GET with @PathParams using java.util.Date failed",
            expected.toGMTString())).getWithDatePathParam(expected);
  }

  @Test
  public void testGetWithMultiplePathParams() {
    call(PathParamTestService.class,
        new SimpleAssertionCallback<>("@GET with @PathParams failed", "1/2")).getWithMultiplePathParams(1, 2);
  }

  @Test
  public void testGetWithReusedPathParam() {
    call(PathParamTestService.class,
        new SimpleAssertionCallback<>("@GET with @PathParam failed", "1.0/2.0/1.0")).getWithReusedPathParam(1.0, 2.0);
  }

  @Test
  public void testPostWithPathParam() {
    call(PathParamTestService.class,
        new SimpleAssertionCallback<>("@POST with @PathParam failed", 1f)).postWithPathParam("", 1f);
  }

  @Test
  public void testPutWithPathParam() {
    call(PathParamTestService.class,
        new SimpleAssertionCallback<>("@PUT with @PathParam failed", 3l)).putWithPathParam(2l, 1l);
  }

  @Test
  public void testDeleteWithPathParam() {
    call(PathParamTestService.class,
        new SimpleAssertionCallback<>("@DELETE with @PathParam failed", 1l)).deleteWithPathParam(1l);
  }

  @Test
  public void testHeadWithPathParam() {
    call(PathParamTestService.class,
        new SimpleAssertionCallback<>("@HEAD with @PathParam failed", null)).headWithPathParam(1l);
  }
}
