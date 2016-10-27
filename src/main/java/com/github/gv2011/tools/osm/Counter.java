package com.github.gv2011.tools.osm;

import static com.github.gv2011.util.ex.Exceptions.format;
import static com.github.gv2011.util.ex.Exceptions.run;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Counter<T> {

  private final String name;
  private final Optional<Path> file;
  private final Map<T,Integer> map = new HashMap<>();

  public Counter(final String name) {
    this.name = name;
    this.file = Optional.empty();
  }

  public Counter(final String name, final Path file) {
    this.name = name;
    this.file = Optional.of(file);
  }

  public int register(final T element){
    final int count = map.getOrDefault(element, 0)+1;
    map.put(element, count);
    if(count==1){
      final String elementStr = element.toString();
      System.out.println(format("New {}: {}", name, elementStr));
      if(file.isPresent()){
        run(()->Files.write(
          file.get(),
          (elementStr+'\n').getBytes(UTF_8),
          StandardOpenOption.APPEND, StandardOpenOption.CREATE
        ));
      }
    }
    return count;
  }

}
