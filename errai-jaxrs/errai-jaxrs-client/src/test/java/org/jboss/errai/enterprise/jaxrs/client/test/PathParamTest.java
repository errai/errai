package org.jboss.errai.enterprise.jaxrs.client.test;

import static org.jboss.errai.enterprise.rebind.JaxrsResourceMethodParameters.getPathParameterExpressions;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public class PathParamTest {
  
  @Test
  public void testMatchPathParamExpressions() {
    final List<String> expected = Arrays.asList("isbn", "param", "name", "zip", "p", "many", "id", "nr");
    
    final String path = "/{isbn}/aaa{param}bbb/{name}-{zip}/aaa{p:b+}/{many:.*}/{id:[0-9]{1}[0-9]{0,}}/{nr:[0-9]*}";
    final List<String> pathParamNames = getPathParameterExpressions(path)
            .stream()
            .map(s -> s.split(":")[0])
            .collect(Collectors.toList());
    
    assertEquals(expected, pathParamNames);
  }

}
