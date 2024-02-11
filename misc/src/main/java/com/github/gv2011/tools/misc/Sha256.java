package com.github.gv2011.tools.misc;

import static com.github.gv2011.util.ex.Exceptions.call;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import com.github.gv2011.util.BeanUtils;
import com.github.gv2011.util.FileUtils;
import com.github.gv2011.util.beans.Bean;
import com.github.gv2011.util.beans.BeanBuilder;
import com.github.gv2011.util.beans.BeanType;
import com.github.gv2011.util.bytes.FileExtension;
import com.github.gv2011.util.bytes.Hash256;
import com.github.gv2011.util.bytes.HashAndSize;
import com.github.gv2011.util.icol.IList;
import com.github.gv2011.util.icol.Opt;
import static java.util.Comparator.comparing;

public final class Sha256 {

  private static final FileExtension SHA_256_EXT = FileExtension.parse("sha256");

  public static enum Code{
    OK,
    HASH_FILE_WRITTEN,
    TARGET_MISSING,
    WRONG_HASH
  }

  public static interface ResultLine extends Bean{
    Code code();
    String filename();
    Opt<Hash256> hash();
    Opt<Long> size();
  }

  void run(final IList<String> args){
    try(Stream<ResultLine> s = run(FileUtils.WORK_DIR)){
      System.out.print("[");
      final AtomicReference<String> sep = new AtomicReference<>("\n");
      final BeanType<ResultLine> rlType = BeanUtils.typeRegistry().beanType(ResultLine.class);
      s
        .sorted(comparing(ResultLine::code).thenComparing(comparing(ResultLine::filename)))
        .forEachOrdered(rl->{
          System.out.print(sep.getAndSet(",\n")+rlType.toJson(rl).serialize());
        })
      ;
      System.out.print("\n]\n");
    }
  }

  Stream<ResultLine> run(final Path dir){
     return call(()->Files
      .list(dir)
      .unordered().parallel()
      .filter(Files::isRegularFile)
      .map(this::basePath)
      .distinct()
      .filter(p->!FileUtils.getExtension(p).equals(SHA_256_EXT))
      .map(this::handleFile)
    );
  }

  private Path basePath(final Path file){
    return FileUtils.getExtension(file).equals(SHA_256_EXT)
      ? FileUtils.withoutExtension(file)
      : file
    ;
  }

  private ResultLine handleFile(final Path basePath){
    final FileExtension ext = FileUtils.getExtension(basePath);
    assert !ext.equals(SHA_256_EXT);
    final Path shaFile = basePath.getParent().resolve(basePath.getFileName()+"."+SHA_256_EXT);
    final BeanBuilder<ResultLine> result = BeanUtils.beanBuilder(ResultLine.class);
    result.set(ResultLine::filename).to(basePath.getFileName().toString());
    if(Files.exists(basePath)){
      final HashAndSize hashAndSize = FileUtils.hash(basePath);
      if(Files.exists(shaFile)){
        result.setOpt(ResultLine::hash).to(hashAndSize.hash());
        result.setOpt(ResultLine::size).to(hashAndSize.size());
        final Hash256 expected = Hash256.parse(FileUtils.readText(shaFile));
        result.set(ResultLine::code).to(hashAndSize.hash().equals(expected) ? Code.OK : Code.WRONG_HASH);
      }
      else{
        FileUtils.writeText(hashAndSize.hash().content().toHex(), shaFile);
        result.setOpt(ResultLine::hash).to(hashAndSize.hash());
        result.setOpt(ResultLine::size).to(hashAndSize.size());
        result.set(ResultLine::code).to(Code.HASH_FILE_WRITTEN);
      }
    }
    return result.build();
  }
}
