package com.github.gv2011.m2t;

import com.github.gv2011.util.StringUtils;
import com.github.gv2011.util.beans.Default;


public enum Type {
  @Default JAR,
  POM, ZIP;

  public static final String M2_NAME = "type";

  @Override
  public String toString(){return StringUtils.toLowerCase(name());}
}
