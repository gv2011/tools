package com.github.gv2011.accounting;

import com.github.gv2011.util.icol.AbstractCachedIList;

public class AccountingList extends AbstractCachedIList<AccountingEntry>{

  private final AccountingEntry last;

  public AccountingList(AccountingEntry last) {
    this.last = last;
  }

  @Override
  public int size() {
    return last.index()+1;
  }

  @Override
  public AccountingEntry get(final int index) {
    AccountingEntry e = last;
    while(e.index()!=index) e = e.previous().get();
    return e;
  }

}
