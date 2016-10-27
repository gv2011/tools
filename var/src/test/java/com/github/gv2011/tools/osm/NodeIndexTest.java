package com.github.gv2011.tools.osm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.junit.Ignore;
import org.junit.Test;

public class NodeIndexTest {

  @Test
  public void testPut() {
    assertThat((int)Math.floor(179.9999999d), is(179) );
    assertThat((int)Math.floor(Math.abs(-179.9999999d)), is(179) );
    assertThat((int)Math.floor(-0.0000001d)+1, is(0) );
    assertThat((int)Math.floor(-0.99999991d)+1, is(0) );
    assertThat(((int)Math.floor(-0.99999991d)+1)/60, is(0) );
  }

  @Test
  @Ignore
  public void testGetPath() {
    final Path base = FileSystems.getDefault().getPath("");
    assertThat(
      new NodeIndex().getPath(base, new Node(12614600L, 0.4477657, 0.5265215)).toString(),
      is("0E-0N")
    );
  }

}
