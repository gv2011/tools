package com.github.gv2011.tools.misc;

import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.ex.Exceptions.illegalArgument;
import static com.github.gv2011.util.icol.ICollections.asList;
import static java.util.function.Predicate.not;

import java.util.List;

import com.github.gv2011.util.icol.IList;

public final class Main {

  public static void main(final String[] args) {
    final IList<String> argsList = asList(args);
    verify(argsList, not(List::isEmpty));
    final String cmd = argsList.first();
    if(cmd.equals("sha256")){
      new Sha256().run(argsList.tail());
    }
    else illegalArgument(cmd);
  }

}
