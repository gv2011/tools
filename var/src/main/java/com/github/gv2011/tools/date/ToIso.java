package com.github.gv2011.tools.date;

import static com.github.gv2011.util.FileUtils.*;
import static com.github.gv2011.util.FileUtils.readText;
import static com.github.gv2011.util.FileUtils.writeText;
import static com.github.gv2011.util.Verify.verify;

import java.util.regex.Pattern;

public class ToIso {

  public static void main(final String[] args) {
    final StringBuilder sb = new StringBuilder();
    for(String line: readText("in.txt").split(Pattern.quote("\n"))){
      line = line.trim();
      verify(line.length()==10);
      sb.append(
       line.substring(6, 10) + "-"
       + line.substring(3, 5) + "-"
       + line.substring(0, 2) + "\n"
     );
    }
    writeText(sb.toString(), path("out.txt"));
  }

}
