package com.github.gv2011.tools.regex;

import static com.github.gv2011.util.StringUtils.readFile;
import static com.github.gv2011.util.ex.Exceptions.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {

  public static void main(final String[] args) {
    final String regex = readFile("regex.txt");
    final Pattern p = Pattern.compile(regex);
    final String text = readFile("in.txt");
    final Matcher matcher = p.matcher(text);
    final boolean matches = matcher.matches();
    System.out.println(format(
      "The string \"{}\" {} the pattern \"{}\".",
      text, matches?"matches":"does not match", regex
    ));
    if(matches){
      for(int i=0; i<=matcher.groupCount(); i++){
        final String group = matcher.group(i);
        final String groupStr = group==null?null:format("\"{}\"", group);
        System.out.println(format("Group {}: {}", i, groupStr));
      }
    }
  }

}

