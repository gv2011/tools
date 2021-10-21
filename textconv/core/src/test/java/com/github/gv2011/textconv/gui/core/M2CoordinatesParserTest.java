package com.github.gv2011.textconv.gui.core;

import org.junit.jupiter.api.Test;

class M2CoordinatesParserTest {

  @Test
  void testParse() {
    final String in =
          "    <dependency>\r\n"
        + "      <groupId>com.github.gv2011</groupId>\r\n"
        + "      <artifactId>tools-textconv-gui</artifactId>\r\n"
        + "      <version>${project.version}</version>\r\n"
        + "    </dependency>\r\n"
        + "    <dependency>\r\n"
        + "      <groupId>com.github.gv2011</groupId>\r\n"
        + "      <artifactId>util-apis</artifactId>\r\n"
        + "    </dependency>\r\n"
        + "";
    System.out.println(new M2CoordinatesParser().parse(in));
  }

}
