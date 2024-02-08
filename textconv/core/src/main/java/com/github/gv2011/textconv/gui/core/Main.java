package com.github.gv2011.textconv.gui.core;

import static com.github.gv2011.util.ex.Exceptions.call;

import java.util.concurrent.CountDownLatch;

import com.github.gv2011.textconv.gui.TextconvGui;

public class Main {

  public static void main(final String[] args) {
    final CountDownLatch countDownLatch = new CountDownLatch(1);
    try(
      TextconvGui gui = TextconvGui.create(
        countDownLatch::countDown,
//        new M2CoordinatesParser()::parse
        new DateConverter()::convert
      )
    ){
      call(()->countDownLatch.await());
    }
  }

}
