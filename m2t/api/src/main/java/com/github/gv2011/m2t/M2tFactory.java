package com.github.gv2011.m2t;

import com.github.gv2011.util.Constant;
import com.github.gv2011.util.serviceloader.RecursiveServiceLoader;

public interface M2tFactory {

  public static final Constant<M2tFactory> INSTANCE = RecursiveServiceLoader.lazyService(M2tFactory.class);

  M2t create();

}
