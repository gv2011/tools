package com.github.gv2011.tools.sys;

import static com.github.gv2011.util.CollectionUtils.toISortedSet;
import static com.github.gv2011.util.ex.Exceptions.format;
import static java.util.stream.Collectors.joining;

import java.nio.charset.Charset;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.gv2011.util.CharacterType;
import com.github.gv2011.util.uc.UChar;

public class CharacterInfo {

  public static void main(final String[] args) {

    for(int i=1; i<=16; i++) if(i!=12 && i!=10 && i!=14 && i!=11 && i!=16){
      final Charset cs = Charset.forName("ISO-8859-"+i);
      for(int j=0; j<256; j++){
        final String ch = new String(new byte[]{(byte)j}, cs);
        final UChar c = UChar.uChar(ch);
        final int cp = c.codePoint();
        if(cp!=j && cp>=256 && !c.equals(UChar.REPLACEMENT_CHARACTER)){
          System.out.println(format(
            "{}: {} '{}' cp:{} {}",
            cs, Integer.toHexString(j), ch, Integer.toHexString(cp), c.name()
          ));
        }
      }
    }

    for(int cp=0; cp<256; cp++){
      final UChar c = UChar.uChar(cp);
      final String display = display(c);
      @SuppressWarnings("unused")
      final String cInfo = format("{}: {} {} ({})", cp, display, c.name(), c.type());
      //System.out.println(cInfo);
    }
    final String byType = IntStream.range(0,128)
      .mapToObj(UChar::uChar)
      .collect(Collectors.groupingBy(
        c->c.type(),
        TreeMap::new,
        toISortedSet()
      ))
      .entrySet().stream()
      .map(e->{
        return
          e.getKey().name() + "\n  " +
          (
            e.getValue().stream()
            .map(c->format(
              "{} {} ({}) {}",
              display(c), c.name(), Integer.toHexString(c.codePoint()), c.printable()
            ))
            .collect(joining("\n  "))
          ) + "\n"
        ;
      })
      .collect(joining("\n"))
    ;
    System.out.println(byType);
  }

  private static String display(final UChar c) {
    return c.type()==CharacterType.CONTROL ? "" : "'"+c+"'";
  }

}
