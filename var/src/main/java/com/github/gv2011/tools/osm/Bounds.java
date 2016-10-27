package com.github.gv2011.tools.osm;

public class Bounds {

  public static final Bounds ALL = new Bounds(-180d, 180d, -90d, +90d);

  private final double minLon;
  private final double maxLon;
  private final double minLat;
  private final double maxLat;

  public Bounds(final double minLon, final double maxLon, final double minLat, final double maxLat) {
    this.minLon = minLon;
    this.maxLon = maxLon;
    this.minLat = minLat;
    this.maxLat = maxLat;
  }

  public double minLon() {
    return minLon;
  }

  public double maxLon() {
    return maxLon;
  }

  public double minLat() {
    return minLat;
  }

  public double maxLat() {
    return maxLat;
  }

  public double aspectRatio() {
    return (maxLon-minLon)/(maxLat-minLat);
  }



}
