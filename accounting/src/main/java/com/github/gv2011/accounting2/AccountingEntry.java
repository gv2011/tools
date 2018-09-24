package com.github.gv2011.accounting2;

import com.github.gv2011.util.icol.Opt;
import com.github.gv2011.util.time.IsoDay;

public interface AccountingEntry {

  Amount amount();

  default Amount balance(){
    return entryBefore().map(AccountingEntry::balance).orElse(Amount.ZERO).add(amount());
  }

  String opposite();

  String message();

  Opt<AccountingEntry> entryBefore();

  IsoDay date();

}
