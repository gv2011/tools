package com.github.gv2011.tools.sort;

import static com.github.gv2011.util.StringUtils.readFile;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;

public class Sort {

  private static final Logger LOG = getLogger(Sort.class);

  private static final String COMMA = "(\\s((*,\\s*)|\\z))?"; //\s*,\s*
  private static final String STRING = "\"(([^\"]|(\\\\\"))*)\""; //"(([^"]|(\\"))*)"
  private static final String REGEX = STRING + COMMA;

  public static void main(final String[] args) {
    System.out.println(REGEX);
    final String txt = readFile("in.txt");
    final Pattern p = Pattern.compile(STRING);
    final Matcher m = p.matcher(txt);
    final SortedMap<String,String> sorted = new TreeMap<>();
    while(m.find()){
      LOG.debug("Found: \"{}\" (starting at index {}).", m.group(), m.start());
      final String rawStr = m.group(1);
      final String str = rawStr.replace("\\\"", "\"").replace("\\\\", "\\");
      sorted.put(str, rawStr);
    };
    System.out.println(sorted.values().stream()
      .map((s)->"\""+s+"\"")
      .collect(Collectors.joining(", "))
    );
  }

}

