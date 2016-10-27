package com.github.gv2011.tools.osm;

public class Node {

  private final long id;
  private final double longitude;
  private final double latitude;



  public Node(final long id, final double longitude, final double latitude) {
    this.id = id;
    this.longitude = longitude;
    this.latitude = latitude;
  }

  public long id() {
    return id;
  }

  public double longitude() {
    return longitude;
  }

  public double latitude() {
    return latitude;
  }


}
