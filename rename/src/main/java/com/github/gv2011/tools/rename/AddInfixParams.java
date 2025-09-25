package com.github.gv2011.tools.rename;

import com.github.gv2011.util.beans.Bean;

public interface AddInfixParams extends Bean{

  String directory();
  String newInfix();
  Boolean apply();

}
