package com.github.gv2011.accounting2;

import java.time.LocalDate;

import com.github.gv2011.util.icol.Opt;

public interface AccountingEntry {

  Amount amount();

  default Amount balance(){
    return entryBefore().map(AccountingEntry::balance).orElse(Amount.ZERO).add(amount());
  }

  String opposite();

  String message();

  Opt<AccountingEntry> entryBefore();

  LocalDate date();

}
