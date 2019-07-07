package com.github.gv2011.m2t;

import com.github.gv2011.util.tstr.TypedString;

public interface ArtifactId extends TypedString<ArtifactId>{

  public static final String M2_NAME = "artifactId";

  static ArtifactId create(final String artifactIdString){
    return TypedString.create(ArtifactId.class, artifactIdString);
  }

}
