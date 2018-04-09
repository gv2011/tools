package com.github.gv2011.accounting;

import java.util.Optional;

import com.github.gv2011.util.time.IsoDay;

public class AccountingEntry {

  private final Amount amount;
  private final Amount balance;
  private final Optional<AccountingEntry> nextEntry;
  private final IsoDay date;
  private final String opposite;
  private final String message;

  public AccountingEntry(
    final IsoDay date,
    final Amount amount,
    final Amount balance,
    final Optional<AccountingEntry> nextEntry, final String opposite, final String message
  ) {
    this.amount = amount;
    this.balance = balance;
    this.nextEntry = nextEntry;
    this.date = date;
    this.opposite = opposite;
    this.message = message;
  }

  public Amount amount() {
    return amount;
  }

  public Amount balance() {
    return balance;
  }

  public String opposite() {
    return opposite;
  }

  public String message() {
    return message;
  }

  public Amount balanceBefore() {
    return balance.subtract(amount);
  }

  public Optional<AccountingEntry> successor(){
    return nextEntry;
  }

  public IsoDay date() {
    return date;
  }

}
