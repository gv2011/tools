package com.github.gv2011.tools.hash;

import static org.junit.Assert.*;

import java.util.Locale;
import java.util.stream.Stream;

import org.junit.Test;

import com.github.gv2011.util.bytes.ByteUtils;
import com.ibm.icu.text.Collator;

public class VerifyHashTest {

  @Test
  public void test3() {
    final Collator collator = Collator.getInstance(Locale.ROOT);
    collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
    collator.setStrength(Collator.IDENTICAL);
//    IntStream.concat(
//      IntStream.range('!','~'),
//      IntStream.of('ẞ','ẞ', '€', 'ä', 'Ä')
//    )   e2 eb c5 20 1 5 1 5 1 21 1 f0 ab 0
    //    4a 4a 1 86 e0 8d 5 1 89 89 bd 1 23 1 d2 92 0
//    IntStream.range(Character.MIN_CODE_POINT, 10000)
//    .filter(c->!Character.isISOControl(c))
//    .mapToObj(Character::toString)
//    Stream.of("s","t","ß")
    Stream.of("s","t","ß","T","ẞ","S")
    .sorted(collator)
//    .sorted()
    .forEach(c->System.out.println(Character.codePointAt(c, 0)+" "+c+" "+
ByteUtils.newBytes(collator.getCollationKey(c).toByteArray())));
  }

}
