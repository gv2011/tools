package com.github.gv2011.tools.sys;

public class ListSystemProperties {

  public static void main(final String[] args) {
    System.getProperties().list(System.out);
  }

}
