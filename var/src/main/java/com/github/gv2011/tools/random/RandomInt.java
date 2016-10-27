package com.github.gv2011.tools.random;

import java.security.SecureRandom;

public class RandomInt {

  public static void main(final String[] args) {
    System.out.println(new SecureRandom().nextInt());
  }

}
