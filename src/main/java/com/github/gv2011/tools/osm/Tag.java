package com.github.gv2011.tools.osm;

import java.util.Optional;

public class Tag extends Element{


  private final String name;
  private final String value;


  public Tag(final Element parent, final String name, final String value) {
    super(Optional.of(parent), "tag", Optional.empty());
    this.name = name;
    this.value = value;
  }

  public String name() {
    return name;
  }

  public String value() {
    return value;
  }

}
