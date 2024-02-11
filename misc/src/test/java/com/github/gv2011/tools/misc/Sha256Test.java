package com.github.gv2011.tools.misc;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class Sha256Test {

  @Test
  void testRunPath() {
    new Sha256().run(Paths.get("work")).forEach(System.out::println);
  }

}
