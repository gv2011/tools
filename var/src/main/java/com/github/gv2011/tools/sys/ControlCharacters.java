package com.github.gv2011.tools.sys;

public class ControlCharacters {

  public static void main(final String[] args) {
    for(int c=0; c<=Character.MAX_CODE_POINT; c++){
      if(Character.getType(c)==Character.CONTROL){
        System.out.println(Integer.toHexString(c)+": "+Character.getName(c));
      }
    }
  }

}
