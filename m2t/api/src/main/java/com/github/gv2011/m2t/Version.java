package com.github.gv2011.m2t;

import com.github.gv2011.util.tstr.TypedString;

public interface Version extends TypedString<Version>{

  public static final String M2_NAME = "version";

  static Version create(final String versionString){
    return TypedString.create(Version.class, versionString);
  }

}
