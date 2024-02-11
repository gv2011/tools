module com.github.gv2011.m2timp{
  requires maven.invoker;
  requires maven.shared.utils;
  requires transitive com.github.gv2011.util;
  requires java.base;
  exports com.github.gv2011.m2timp to com.github.gv2011.util;
  provides com.github.gv2011.util.m2t.M2tFactory with com.github.gv2011.m2timp.M2tFactoryImp;
}