package com.github.gv2011.textconv.gui;

import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.staticClass;

import java.awt.EventQueue;
import java.util.concurrent.atomic.AtomicReference;

import com.github.gv2011.util.ex.ThrowingSupplier;

public final class SwingUtils {

  private SwingUtils(){staticClass();}

  public static final <T> T callSwing(final ThrowingSupplier<? extends T> operation){
    final AtomicReference<T> ref = new AtomicReference<T>();
    call(()->EventQueue.invokeAndWait(()->{
      ref.set(call(operation));
    }));
    return ref.get();
  }

}
