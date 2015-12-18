package org.jboss.errai.jpa.test.client;

import org.jboss.errai.jpa.client.local.JsonUtil;
import org.jboss.errai.jpa.test.client.res.JpaClientTestCase;

import com.google.gwt.json.client.JSONNull;

public class JsonUtilTest extends JpaClientTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.jpa.test.JpaTest";
  }

  public void testBasicValueNullFromJson() throws Exception {
    assertNull(JsonUtil.basicValueFromJson(null, boolean.class));
  }

  public void testNullToBasicJsonValue() throws Exception {
    assertSame(JSONNull.getInstance(), JsonUtil.basicValueToJson(null));
  }
}
