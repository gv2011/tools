module com.github.gv2011.m2timp{
  requires maven.invoker;
  requires maven.shared.utils;
  requires com.github.gv2011.util;
  provides com.github.gv2011.util.m2t.M2tFactory with com.github.gv2011.m2timp.M2tFactoryImp;
}