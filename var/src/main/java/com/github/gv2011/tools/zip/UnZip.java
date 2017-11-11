package com.github.gv2011.tools.zip;

import static com.github.gv2011.util.FileUtils.path;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.gv2011.util.FileUtils;

public class UnZip {

  public static void main(String[] args) throws IOException {
    args = new String[]{"src.zip", "test"};
    final Path targetFolder = path(args[1]);
    Files.createDirectories(targetFolder);
    FileUtils.unZip(path(args[0]), targetFolder);
  }

}
