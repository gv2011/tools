package com.github.gv2011.tools.txtfile;

import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.ex.Exceptions.callWithCloseable;
import static com.github.gv2011.util.ex.Exceptions.format;
import static com.github.gv2011.util.icol.ICollections.nothing;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.gv2011.util.FileUtils;
import com.github.gv2011.util.bytes.ByteUtils;
import com.github.gv2011.util.bytes.Bytes;

public class ReplaceLineEndings {

  public static void main(final String[] args) {
    final Path dir =
      //Paths.get(ConsoleUtils.readLine())
      Paths.get("D:\\data\\Firma\\Kunden\\Mindsquare-50Hz\\src\\sld-builder-data\\50Hertz\\output\\substations")
    ;
    verify(Files.isDirectory(dir));
    System.out.println(dir+": ");
    callWithCloseable(
      ()->Files.list(dir),
      s->{
        s.sorted().forEachOrdered(f->{
          System.out.print(f.getFileName()+": ");
          if(FileUtils.isTextFile(f)){
            Bytes bytes = FileUtils.read(f);
            AtomicInteger count = new AtomicInteger();
            Bytes modified = bytes.replaceAll(ByteUtils.asUtf8("\r\n").content(), ByteUtils.asUtf8("\n").content(), p->count.incrementAndGet());
            if(count.get()>0){
              verify(!modified.equals(bytes));
              modified.write(f);
              System.out.println(format("{} replacements", count.get()));
            }
            else{
              verify(modified.equals(bytes));
              System.out.println("no replacements");
            }
          }
          else System.out.println("not a text file");
        });
        return nothing();
      }
    );
  }

}
