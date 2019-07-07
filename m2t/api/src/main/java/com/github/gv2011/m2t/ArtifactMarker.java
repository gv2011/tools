package com.github.gv2011.m2t;

import java.net.URL;

public interface ArtifactMarker {

  Module module();

  Package basePackage();

  ArtifactRef artifactRef();


  static ArtifactMarker getMarker(final Class<?> clazz, final URL pomPropertiesResource){
    return ArtifactMarkerImp.getMarker(clazz, pomPropertiesResource);
  }



}
