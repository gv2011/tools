package com.github.gv2011.accounting;

import static com.github.gv2011.util.Verify.verifyEqual;

import java.math.BigDecimal;

import com.github.gv2011.util.tstr.AbstractTypedString;

public class Amount extends AbstractTypedString<Amount>{


  public static final Amount ZERO = new Amount(0);

  public static Amount parseComma(String amountStr){
    amountStr = amountStr.replace(".", "");
    final int ci = amountStr.indexOf(',');
    if(ci==-1) {
      amountStr = amountStr+"00";
    } else{
      verifyEqual(ci, amountStr.length()-3);
      amountStr = amountStr.replace(",", "");
    }
    return new Amount(Long.parseLong(amountStr));
  }

  private final long cents;

  private Amount(final long cents) {
    this.cents = cents;
  }

  @Override
  public String toString() {
    String result = Long.toString(Math.abs(cents));
    while(result.length()<3) result = "0"+result;
    return (cents<0 ? "-" : "") + result.substring(0, result.length()-2) + "," + result.substring(result.length()-2);
  }

  @Override
  public int compareWithOtherOfSameType(final Amount o) {
    return Long.compare(cents, o.cents);
  }



  @Override
  public Amount self() {
    return this;
  }

  @Override
  public Class<Amount> clazz() {
    return Amount.class;
  }

  public Amount subtract(final Amount amount) {
    return new Amount(cents - amount.cents);
  }

  public Amount negate() {
    return new Amount(-cents);
  }

  public BigDecimal asBigDecimal() {
    return BigDecimal.valueOf(cents, 2);
  }

  public Number asNumber() {
    return new AmountNumber();
  }

  private final class AmountNumber extends Number {
    @Override
    public double doubleValue() {
      return asBigDecimal().doubleValue();
    }
    @Override
    public float floatValue() {
      return asBigDecimal().floatValue();
    }
    @Override
    public int intValue() {
      return asBigDecimal().intValue();
    }
    @Override
    public long longValue() {
      return asBigDecimal().longValue();
    }
    @Override
    public String toString() {
      return Amount.this.toString();
    }
  }

}
