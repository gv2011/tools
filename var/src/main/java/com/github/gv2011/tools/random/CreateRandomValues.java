package com.github.gv2011.tools.random;

import java.security.SecureRandom;
import java.util.UUID;

public class CreateRandomValues {

  public static void main(final String[] args) {
    final SecureRandom random = new SecureRandom();
    System.out.println(random.nextInt());
    System.out.println(UUID.randomUUID());
  }

}
