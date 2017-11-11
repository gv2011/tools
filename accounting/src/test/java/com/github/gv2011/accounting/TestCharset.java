package com.github.gv2011.accounting;

import java.nio.charset.Charset;

import org.junit.After;
import org.junit.Test;

import com.github.gv2011.util.FileUtils;
import com.github.gv2011.util.bytes.ByteUtils;

public class TestCharset {

  @After
  public void tearDown() throws Exception {}

  @Test
  public void test() {
    ;
    System.out.println(ByteUtils.newFileBytes(FileUtils.path("store", "opposites.csv")).toString(Charset.defaultCharset()));
  }

}
