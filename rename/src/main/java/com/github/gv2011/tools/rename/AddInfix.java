package com.github.gv2011.tools.rename;

import static com.github.gv2011.util.FileUtils.getExtension;
import static com.github.gv2011.util.FileUtils.withoutExtension;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.callWithCloseable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.github.gv2011.util.BeanUtils;

public class AddInfix {

  public static void main(final String[] args) {
    final AddInfixParams p = BeanUtils.parse(AddInfixParams.class, Paths.get("add-infix.json"));
    callWithCloseable(
      ()->Files.list(Paths.get(p.directory())),
      s->{
        s.forEach(path->{
          final String newName = withoutExtension(path).getFileName()+p.newInfix()+"."+getExtension(path);
          if(p.apply()){
            final Path newFile = path.getParent().resolve(newName);
            call(()->Files.move(path, newFile, StandardCopyOption.ATOMIC_MOVE));
            System.out.println("Renamed " + path.getFileName()+" to "+newName);
          }
          else{
            System.out.println(path.getFileName()+" -> "+newName);
          }
        });
      }
    );
  }

}
