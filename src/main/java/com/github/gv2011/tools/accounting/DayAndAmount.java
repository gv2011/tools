package com.github.gv2011.tools.accounting;

import static com.github.gv2011.util.StringUtils.alignRight;

import java.math.BigDecimal;

import com.github.gv2011.util.IsoDay;

public class DayAndAmount implements Comparable<DayAndAmount>{

    public static String formatAmt(final BigDecimal amount){
      final StringBuilder result = new StringBuilder(amount.toString());
      if(amount.scale()==0) result.append(".00");
      else if(amount.scale()==1) result.append("0");
      result.append(" €");
      return alignRight(result, 12);
    }

    IsoDay day;
    BigDecimal amount;

    public DayAndAmount(final IsoDay day, final BigDecimal amount) {
      this.day = day;
      this.amount = amount;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((amount == null) ? 0 : amount.hashCode());
      result = prime * result + ((day == null) ? 0 : day.hashCode());
      return result;
    }
    @Override
    public boolean equals(final Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      final DayAndAmount other = (DayAndAmount) obj;
      if (amount == null) {
        if (other.amount != null) return false;
      } else if (!amount.equals(other.amount)) return false;
      if (day == null) {
        if (other.day != null) return false;
      } else if (!day.equals(other.day)) return false;
      return true;
    }
    @Override
    public String toString() {
      return day + ":" + amount ;
    }
    @Override
    public int compareTo(final DayAndAmount o) {
      int result = day.compareTo(o.day);
      if(result==0) result = amount.compareTo(o.amount);
      return result;
    }
    public IsoDay day() {
      return day;
    }
    public BigDecimal amount() {
      return amount;
    }

  }

