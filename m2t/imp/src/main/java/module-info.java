module com.github.gv2011.m2t.imp{
  requires com.github.gv2011.m2t;
  requires maven.invoker;
  requires maven.shared.utils;
  requires com.github.gv2011.util;
  provides com.github.gv2011.m2t.M2tFactory with com.github.gv2011.m2t.imp.M2tFactoryImp;
}