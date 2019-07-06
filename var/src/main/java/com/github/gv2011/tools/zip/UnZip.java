package com.github.gv2011.tools.zip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.gv2011.util.FileUtils;

public class UnZip {

  public static void main(String[] args) throws IOException {
    args = new String[]{"src.zip", "test"};
    final Path targetFolder = Paths.get(args[1]);
    Files.createDirectories(targetFolder);
    FileUtils.unZip(Paths.get(args[0]), targetFolder);
  }

}
