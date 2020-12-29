package com.github.gv2011.tools.hash;

import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.notYetImplemented;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

import com.github.gv2011.util.FileUtils;
import com.github.gv2011.util.icol.ICollections;
import com.github.gv2011.util.icol.IMap;

public class VerifyHash {

  @SuppressWarnings("unused")
  private static final IMap<String,String> ALGORITHMS = ICollections.<String,String>mapBuilder()
    .put("sha256", "SHA-256")
    .build()
  ;

  public static void main(final String[] args) {
    final Path file = Paths.get("C:\\Dateien\\Software\\java-13-openjdk-13.0.1.9-1.windows.ojdkbuild.x86_64.zip");
    final Path hashFile = findHashFile(file);
    final String algorithm = getAlgorithmByFileName(hashFile);
    @SuppressWarnings("unused")
    final MessageDigest md = call(()->MessageDigest.getInstance(algorithm));
  }

  private static String getAlgorithmByFileName(final Path file) {
    FileUtils.getExtension(file);
    return notYetImplemented();
  }

  private static Path findHashFile(final Path file) {
    return FileUtils.list(file.getParent()).filter(f->isHashFileFor(file, f)).findSingle();
  }

  private static boolean isHashFileFor(final Path file, final Path hashFileCandidate){
    return
      hashFileCandidate.getFileName().toString().startsWith(file.getFileName()+".") &&
      !Files.isDirectory(hashFileCandidate)
    ;
  }

}
