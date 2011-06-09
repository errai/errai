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
package org.jboss.errai.tools.source.server;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Aug 16, 2010
 */
public class Formatter {

  public static void main(String[] args) {
    String s = "@Service(\"calculator\")\n" +
        "@ApplicationScoped\n" +
        "public class CalculatorService implements MessageCallback\n" +
        "{\n" +
        "\n" +
        "  private static final Logger log =\n" +
        "      LoggerFactory.getLogger(CalculatorService.class);\n" +
        "\n" +
        "  @Inject\n" +
        "  MessageBus bus;\n" +
        "\n" +
        "  @Inject\n" +
        "  Calculator calculator;\n" +
        "\n" +
        "  public void callback(Message message)\n" +
        "  {\n" +
        "    log.debug(\"CalculatorService received: \"+message);\n" +
        "    \n" +
        "    if(null==calculator)\n" +
        "      new RuntimeException(\"Not CDI managed\").printStackTrace();\n" +
        "\n" +
        "    Long a = message.get(Long.class, \"a\");\n" +
        "    Long b = message.get(Long.class, \"b\");\n" +
        "\n" +
        "    Long result = calculator.add(a, b);\n" +
        "\n" +
        "    MessageBuilder.createConversation(message)\n" +
        "        .subjectProvided()\n" +
        "        .signalling()\n" +
        "        .with(\"result\", result)\n" +
        "        .noErrorHandling()\n" +
        "        .sendNowWith(bus);\n" +
        "  }\n" +
        "}";
    System.out.println(
        JavaToHTML.format(s)
    );
  }

}

