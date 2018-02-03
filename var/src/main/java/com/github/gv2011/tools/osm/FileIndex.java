package com.github.gv2011.tools.osm;

import static com.github.gv2011.util.FileUtils.delete;
import static com.github.gv2011.util.FileUtils.tryReadText;
import static com.github.gv2011.util.FileUtils.writeText;
import static com.github.gv2011.util.ex.Exceptions.call;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.github.gv2011.util.AutoCloseableNt;
import com.github.gv2011.util.bytes.ByteUtils;
import com.github.gv2011.util.bytes.Hash256;

public class FileIndex {

  private static final int LEVELS = 3;
  private final Object lock = new Object();
  private final Set<Path> fileLocks = new HashSet<>();
  private final Path baseDir;


  public FileIndex(final Path baseDir) {
    this.baseDir = baseDir;
  }

  public Optional<String> put(final String opaqueKey, final String value){
    final Hash256 hash = ByteUtils.hash(opaqueKey);
    final Path path = getPath(hash);
    try(AutoCloseableNt lock = getLock(path)){
      final Optional<String> result = tryReadText(path);
      call(()->Files.createDirectories(path.getParent()));
      writeText(value, path);
      return result;
    }
  }

  public Optional<String> get(final String opaqueKey){
    final Hash256 hash = ByteUtils.hash(opaqueKey);
    final Path path = getPath(hash);
    try(AutoCloseableNt lock = getLock(path)){
      final Optional<String> result = tryReadText(path);
      return result;
    }
  }

  public Optional<String> remove(final String opaqueKey){
    final Hash256 hash = ByteUtils.hash(opaqueKey);
    final Path path = getPath(hash);
    try(AutoCloseableNt lock = getLock(path)){
      final Optional<String> result = tryReadText(path);
      if(result.isPresent()) delete(path);
      return result;
    }
  }

  private AutoCloseableNt getLock(final Path path) {
    synchronized(lock){
      while(fileLocks.contains(path)) call(()->lock.wait());
      fileLocks.add(path);
    }
    return ()->{
      synchronized(lock){
        fileLocks.remove(path);
      }
    };
  }

  private Path getPath(final Hash256 hash) {
    final String hexString = hash.toString();
    Path dir = baseDir;

    for(int i=0; i<LEVELS; i++) dir = dir.resolve(hexString.substring(i*2, i*2+2));
    dir = dir.resolve(hexString.substring(LEVELS*2)+".txt");
    return dir;
  }


}
