package com.github.gv2011.tools.zip;

import static com.github.gv2011.util.FileUtils.path;

import com.github.gv2011.util.FileUtils;

public class Zip {

  public static void main(String[] args) {
    args = new String[]{"src", "src.zip"};
    FileUtils.zip(path(args[0]), path(args[1]));
  }

}
