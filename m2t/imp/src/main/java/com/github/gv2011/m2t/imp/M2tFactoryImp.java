package com.github.gv2011.m2t.imp;

import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.Invoker;

import com.github.gv2011.m2t.M2t;
import com.github.gv2011.m2t.M2tFactory;

public class M2tFactoryImp implements M2tFactory{

  @Override
  public M2t create() {
    final Invoker invoker = new DefaultInvoker();
    return new M2tImp(invoker);
  }

}
