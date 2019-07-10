package com.github.gv2011.m2t;

import com.github.gv2011.util.tstr.TypedString;

public interface Classifier extends TypedString<Classifier>{

  public static final String M2_NAME = "classifier";

  static Classifier create(final String artifactIdString){
    return TypedString.create(Classifier.class, artifactIdString);
  }

}
