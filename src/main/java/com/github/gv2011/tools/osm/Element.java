package com.github.gv2011.tools.osm;

import java.util.Optional;

public class Element {

  private final Optional<Element> parent;
  private final String name;
  private final Optional<Long> id;



  public Element(final Optional<Element> parent, final String name, final Optional<Long> id) {
    this.parent = parent;
    this.name = name;
    this.id = id;
  }

  public Optional<Element> parent() {
    return parent;
  }

  public String eName() {
    return name;
  }

  public Optional<Long> id() {
    return id;
  }

}
