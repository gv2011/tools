package com.github.gv2011.textconv.gui.core;

import static java.util.stream.Collectors.joining;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.github.gv2011.util.StringUtils;

public class DateConverter {

  @SuppressWarnings("unused")
  private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ROOT);

  public String convert(final String dateList){
    return StringUtils
      .split(dateList, '\n').stream()
      .map(String::trim)
      .filter(s->!s.isEmpty())
      .map(this::convertDate)
      .collect(joining("\n"))
    ;
  }

  private String convertDate(final String s){
    //return LocalDate.parse(s, FORMAT).toString();
    return s.substring(s.length()-6);
  }

}
