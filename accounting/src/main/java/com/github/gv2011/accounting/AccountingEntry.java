package com.github.gv2011.accounting;

import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.Verify.verifyEqual;

import java.time.LocalDate;
import java.util.Optional;

public class AccountingEntry {

  private final int id;
  private final int index;
  private final Amount amount;
  private final Amount balance;
  private final Optional<AccountingEntry> previous;
  private final LocalDate date;
  private final String opposite;
  private final String message;
  private final int offset;

  public AccountingEntry(
    final LocalDate date,
    final Amount amount,
    final Amount balance,
    final Optional<AccountingEntry> previous, 
    final String opposite, 
    final String message,
    final int offset
  ) {
    verify(offset>=1);
    this.id = previous.map(p->p.id()+offset).orElse(offset-1);
    this.index = previous.map(p->p.index()+1).orElse(0);
    this.amount = amount;
    this.balance = balance;
    this.previous = previous;
    this.date = date;
    this.opposite = opposite;
    this.message = message;
    this.offset = offset;
    previous.ifPresent(p->verify(date, d->!d.isBefore(p.date())));
    verifyEqual(balance, previous.map(AccountingEntry::balance).orElse(Amount.ZERO).add(amount));
  }

  public int id() {
    return id;
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

  public Optional<AccountingEntry> previous(){
    return previous;
  }

  public AccountingEntry first(){
    AccountingEntry first = this;
    while(first.previous().isPresent()) first = first.previous().get();
    return first;
  }

  public LocalDate date() {
    return date;
  }
  
  public int index() {
    return index;
  }
  
  public int offset() {
    return offset;
  }
  
}
