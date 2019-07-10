package com.github.gv2011.tools.zip;

import java.nio.file.Paths;

import com.github.gv2011.util.FileUtils;

public class Zip {

  public static void main(String[] args) {
    args = new String[]{"src", "src.zip"};
    FileUtils.zip(Paths.get(args[0]), Paths.get(args[1]));
  }

}
