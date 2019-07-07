package com.github.gv2011.m2t;

public interface ArtifactMarker {

  public static final String POM_PROPERTIES = "pom.properties";

  Module module();

  Package basePackage();

  ArtifactRef artifactRef();

}
