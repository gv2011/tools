package com.github.gv2011.tools.accounting;

import static com.github.gv2011.util.Verify.verify;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;

class BankingCheck {

  private final BigDecimal expected;
  private final SortedMap<String, SortedMap<String, Object>> entries;
  private final Set<String> matched = new HashSet<>();
  private final Set<String> notMatched;
  private BigDecimal sum = BigDecimal.ZERO;

  BankingCheck(final BigDecimal expectedBalance, final SortedMap<String, SortedMap<String, Object>> bankEntries) {
    expected = expectedBalance;
    entries = bankEntries;
    notMatched = new HashSet<>(entries.keySet());
    BigDecimal sum = BigDecimal.ZERO;
    for(final SortedMap<String, Object> e: entries.values()){
      final BigDecimal amount = (BigDecimal)e.get("amount");
      verify(!amount.equals(BigDecimal.ZERO));
      sum = sum.add(amount);
    }
    verify(sum.equals(expected));
  }

  public void matched(final String id, final BigDecimal amount) {
    verify(!matched.contains(id));
    verify(notMatched.contains(id));
    final Object expectedAmount = entries.get(id).get("amount");
    verify(amount.equals(expectedAmount), "Id: {}, expected: {}, found: {}.", id, expectedAmount, amount);
    matched.add(id);
    notMatched.remove(id);
    sum  = sum.add(amount);
  }

  public void checkBalance() {
    verify(notMatched.isEmpty());
    verify(sum.equals(expected));
  }

  public void checkBalance(final BigDecimal balanceInBank, final BigDecimal balanceOutBank) {
    final BigDecimal surplus = balanceInBank.subtract(balanceOutBank);
    verify(
      surplus.equals(expected),
      "{} - {} = {}, expected {}", balanceInBank, balanceOutBank, surplus, expected);
  }

}
