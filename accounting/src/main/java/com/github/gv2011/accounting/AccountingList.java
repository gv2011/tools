package com.github.gv2011.accounting;

import com.github.gv2011.util.icol.AbstractCachedIList;

public class AccountingList extends AbstractCachedIList<AccountingEntry>{

  private final AccountingEntry root;
  private final int size;

  public AccountingList(AccountingEntry root) {
    this.root = root;
    int count = 1;
    while(root.successor().isPresent()){
      root = root.successor().get();
      count++;
    }
    size = count;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public AccountingEntry get(final int index) {
    AccountingEntry e = root;
    for(int i=0; i<index; i++) e = e.successor().get();
    return e;
  }

}
