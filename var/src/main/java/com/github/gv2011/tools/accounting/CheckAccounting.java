package com.github.gv2011.tools.accounting;

import static com.github.gv2011.util.FileUtils.getPath;
import static com.github.gv2011.util.SetUtils.intersection;
import static com.github.gv2011.util.SetUtils.unique;
import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.ex.Exceptions.format;
import static com.github.gv2011.tools.accounting.DayAndAmount.formatAmt;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.github.gv2011.tools.accounting.XlsUtils.XlsSheet;
import com.github.gv2011.util.IsoDay;
import com.github.gv2011.util.ann.Nullable;

public class CheckAccounting {

  public static void main(final String[] args){
    new CheckAccounting().processAndUpdate();
  }

  private static enum Column{
    DAY,ID,PEER_ACC,SUBJECT_ACC,TYPE,AMOUNT,VAT,NET,TAX_RATE,BANK,FLAG,COMMENT,PEER,SUBJECT,SECTION
  }

  private static enum Section{IN, OUT, TAX, OTHER;
    BigDecimal factor(){return this==IN?BigDecimal.ONE:BigDecimal.ONE.negate();}
  }

  public void processAndUpdate(){
    BigDecimal balanceIn = BigDecimal.ZERO;
    BigDecimal balanceInBank = BigDecimal.ZERO;
    BigDecimal balanceOut = BigDecimal.ZERO;
    BigDecimal balanceOutBank = BigDecimal.ZERO;
    BigDecimal balanceTax = BigDecimal.ZERO;
    BigDecimal privatePayments = BigDecimal.ZERO;
    BigDecimal privateOut = BigDecimal.ZERO;
    BigDecimal incomeTaxOut = BigDecimal.ZERO;
    BigDecimal cashOut = BigDecimal.ZERO;
    final SortedMap<Integer,SortedMap<Column,Object>> table;
    final SortedMap<String, SortedMap<String, Object>> bankEntries = new ReadBank().read();
    final BankingCheck bankingCheck = new BankingCheck(new BigDecimal("31810.72"), bankEntries);
    final SortedSet<String> mappedBankEntries = new TreeSet<>();
    try(XlsSheet xs = XlsUtils.open(getPath("data", "EÜR.xls"), getPath("data","EÜR-mod.xls"))){
      final HSSFSheet sheet = xs.get();
      table = new CheckAccounting().readAccounting(xs);
      final Set<DayAndAmount> uniqueDams = getUniqueDams(bankEntries.values(), table.values());
      System.out.println("uniqueDams: "+uniqueDams.size());
      final Set<BigDecimal> uniqueAmounts = getUniqueAmounts(bankEntries.values(), table.values());
      System.out.println("uniqueAmounts: "+uniqueDams.size());
      for(final Entry<Integer, SortedMap<Column, Object>> e: table.entrySet()) {
        final int rowNo = e.getKey();
        final SortedMap<Column, Object> row = e.getValue();
        final Section section = (Section) row.get(Column.SECTION);
        final String type = (String) row.get(Column.TYPE);
        final BigDecimal amount =  (BigDecimal) row.get(Column.AMOUNT);
//        System.out.print(format("{}: {}{}", rowNo, section, formatAmt(amount)));
        final BigDecimal neutralAmount = amount.multiply(section.factor());
        @Nullable String id = (String) row.get(Column.ID);
        if(!bankEntries.containsKey(id)){
          id=null;
          final IsoDay day = (IsoDay) row.get(Column.DAY);
          final DayAndAmount dam = new DayAndAmount(day, neutralAmount);
          if(uniqueAmounts.contains(neutralAmount)) {
            id = getId(bankEntries, neutralAmount);
          }
          else if(uniqueDams.contains(dam)) {
            id = getId(bankEntries, dam);
          }
        }
        final boolean matched = id!=null;
        BigDecimal bankAmount;
        if(matched){
          bankAmount = (BigDecimal) row.get(Column.BANK);
          if(bankAmount.equals(BigDecimal.ZERO)) bankAmount = amount;
          final BigDecimal neutralBankAmount = bankAmount.multiply(section.factor());
//          System.out.println(format(" matched: {},{}",id,formatAmt(neutralBankAmount)));
          final boolean added = mappedBankEntries.add(id);
          if(!added)throw new RuntimeException(format("{} mapped before.", id));
          bankingCheck.matched(id, neutralBankAmount);
          final HSSFRow xlsRow = sheet.getRow(rowNo);
          setCell(xlsRow, Column.ID, id);
          updateAmount(section, xlsRow, (BigDecimal)bankEntries.get(id).get("amount"));
          setCell(xlsRow, Column.PEER, (String)bankEntries.get(id).get("peer"));
          setCell(xlsRow, Column.SUBJECT, (String)bankEntries.get(id).get("subject"));

          final BigDecimal privatePayment;
          if(type.equals("PZ") || type.equals("KKA"))  privatePayment = bankAmount;
          else if(section==Section.OUT && type.isEmpty() && bankAmount.compareTo(amount)>0)
                                                       privatePayment = bankAmount.subtract(amount);
          else                                         privatePayment = BigDecimal.ZERO;
          if(!privatePayment.equals(BigDecimal.ZERO)){
            System.out.println(format(" private payment: {}, {}",formatAmt(privatePayment), rowNo));
            privatePayments = privatePayments.add(privatePayment);
          }

          if(type.equals("E/E")) {
            verify(amount.equals(BigDecimal.ZERO));
            privateOut = privateOut.add(bankAmount);
          }

          if(type.equals("EST")) {
            verify(amount.equals(BigDecimal.ZERO));
            incomeTaxOut = incomeTaxOut.add(bankAmount);
          }

          if(section==Section.IN)       balanceInBank  = balanceInBank .add(bankAmount);
          else if(section==Section.OUT) balanceOutBank = balanceOutBank.add(bankAmount);
          else if(section==Section.TAX) balanceOutBank = balanceOutBank.add(bankAmount);
        }else{
          bankAmount = BigDecimal.ZERO;
//          System.out.println();
        }

        if(section==Section.IN){
          balanceIn = balanceIn.add(amount);
        }
        else if(section==Section.OUT){
          balanceOut = balanceOut.add(amount);
          if(type.equals("BAR"))cashOut = cashOut.add(amount.subtract(bankAmount));
        }
        else if(section==Section.TAX){
          balanceTax = balanceTax.add(amount);
        }
        else throw new RuntimeException();
      }
    }
    writeMissingBankEntries(bankEntries, mappedBankEntries);
    bankingCheck.checkBalance();
    bankingCheck.checkBalance(balanceInBank, balanceOutBank);
    System.out.println("Earnings:             "+formatAmt(balanceIn));
    System.out.println("Earnings bank:        "+formatAmt(balanceInBank));
    System.out.println("Expenses:             "+formatAmt(balanceOut));
    System.out.println("Expenses bank:        "+formatAmt(balanceOutBank));
    System.out.println("VAT paid:             "+formatAmt(balanceTax));
    System.out.println("Private payments:     "+formatAmt(privatePayments));
    System.out.println("Private transfers out:"+formatAmt(privateOut));
    System.out.println("Income tax out:       "+formatAmt(incomeTaxOut));
    System.out.println("Cash out:             "+formatAmt(cashOut));
    final BigDecimal bPriv =
      privatePayments
      .subtract(cashOut)
      .add(privateOut)
      .add(incomeTaxOut)
    ;
    System.out.println("Balance private out:  "+formatAmt(bPriv ));
    }

  private void updateAmount(final Section section, final HSSFRow xlsRow, final BigDecimal neutralBankAmount) {
    final int column = Column.BANK.ordinal();
    final BigDecimal sectionAmount = neutralBankAmount.multiply(section.factor());
    HSSFCell cell = xlsRow.getCell(column);
    if(cell==null) cell = xlsRow.createCell(column, Cell.CELL_TYPE_NUMERIC);
    else{
      final BigDecimal actual = XlsUtils.readNumber(cell);
      if(!(actual.abs().equals(sectionAmount.abs()) || actual.equals(BigDecimal.ZERO))){
        throw new IllegalStateException(
          format("Row {}, found {}, expected {}.",xlsRow.getRowNum(), actual, sectionAmount)
        );
      }
    }
    final BigDecimal accountingAmount = XlsUtils.readNumber(xlsRow.getCell(amountColumn(section).ordinal()));
    if(accountingAmount.equals(sectionAmount))cell.setCellType(Cell.CELL_TYPE_BLANK);
    else cell.setCellValue(sectionAmount.doubleValue());
  }

  private static void setCell(final HSSFRow row, final Column column, final String value) {
    HSSFCell cell = row.getCell(column.ordinal());
    if(cell==null) cell = row.createCell(column.ordinal(), Cell.CELL_TYPE_STRING);
    cell.setCellValue(value);
  }

  private static void writeMissingBankEntries(final SortedMap<String, SortedMap<String, Object>> bankEntries,
      final SortedSet<String> mappedBankEntries) {
    final AtomicInteger row = new AtomicInteger(0);
    try(XlsSheet xs = XlsUtils.createEmpty(getPath("data", "bank-missing.xls"))){
      //final BigDecimal limit = BigDecimal.valueOf(1000L);
      bankEntries.entrySet().stream()
        .filter(e->!mappedBankEntries.contains(e.getKey()))
        //.filter(e->((BigDecimal)e.getValue().get("amount")).abs().compareTo(limit)>=0)
        .forEach(e->{
          writeMissing(xs, row.getAndIncrement(), e.getValue());
        })
      ;
      }
  }

  private static void writeMissing(final XlsSheet sheet, final int rownum, final SortedMap<String, Object> entry) {
    final Row row = sheet.get().createRow(rownum);
    createStringCell(row, Column.DAY, entry.get("day"));
    createStringCell(row, Column.ID, entry.get("key"));
    createStringCell(row, Column.PEER_ACC, entry.get("peer"));
    row.createCell(Column.BANK.ordinal(), Cell.CELL_TYPE_NUMERIC).setCellValue(((BigDecimal)entry.get("amount")).doubleValue());
    createStringCell(row, Column.FLAG, "B");
    createStringCell(row, Column.COMMENT, "privat");
    createStringCell(row, Column.PEER, entry.get("peer"));
    createStringCell(row, Column.SUBJECT, entry.get("subject"));
 }

  private static void createStringCell(final Row row, final Column column, final Object value) {
    row.createCell(column.ordinal(), Cell.CELL_TYPE_STRING).setCellValue((String) value);
  }

  SortedMap<Integer,SortedMap<Column,Object>> readAccounting(final XlsSheet sheet){
      final SortedMap<Integer,SortedMap<Column,Object>> table = new TreeMap<>();
      HSSFRow row;
      HSSFCell cell;
      final int rows = sheet.get().getPhysicalNumberOfRows();
      verify(rows>0);
      final Set<Section> done = new HashSet<>();
      Section section = Section.OTHER;
      for(int r = 0; r < rows; r++) {
          row = sheet.get().getRow(r);
          if(row != null) {
            cell = row.getCell(Column.DAY.ordinal());
            if(cell==null?false:!cell.toString().trim().isEmpty()){
              final String value = cell.getStringCellValue().trim();
              Section newSection;
              if("Einnahmen:".equals(value)) newSection = Section.IN;
              else if("Summe Einnahmen:".equals(value)) newSection = Section.OTHER;
              else if("Ausgaben:".equals(value)) newSection = Section.OUT;
              else if("Summe Ausgaben:".equals(value)) newSection = Section.OTHER;
              else if("Zahlungen Umsatzsteuer:".equals(value)) newSection = Section.TAX;
              else if("Summe Ust.".equals(value)) newSection = Section.OTHER;
              else newSection = section;
              if(newSection!=section){
                if(done.contains(newSection)) newSection = Section.OTHER;
                done.add(section);
                section = newSection;
              }
              if(section!=Section.OTHER && (value.isEmpty()?false:Character.isDigit(value.charAt(0)))){
                final SortedMap<Column,Object> entry = new TreeMap<>();
                entry.put(Column.SECTION, section);
                final IsoDay day = new IsoDay(value);
                entry.put(Column.DAY, day);
                final HSSFCell idCell = row.getCell(Column.ID.ordinal());
                final String id = idCell==null?"":idCell.getStringCellValue();
                entry.put(Column.ID, id);
                entry.put(Column.TYPE, getString(row, Column.TYPE));
                final Column column = amountColumn(section);
                final BigDecimal amount = XlsUtils.readNumber(row.getCell(column.ordinal()));
                entry.put(Column.AMOUNT, amount);
                final BigDecimal bankAmount = XlsUtils.readNumber(row.getCell(Column.BANK.ordinal()));
                entry.put(Column.BANK, bankAmount);
                table.put(r, entry);
              }
            }
          }
        }
      return table;
    }

  private String getString(final HSSFRow row, final Column column) {
    return Optional.ofNullable(row.getCell(column.ordinal())).map(HSSFCell::getStringCellValue).orElse("");
  }

  private Column amountColumn(final Section section) {
    return section==Section.TAX ? Column.VAT : Column.AMOUNT;
  }

  private static String getId(
    final SortedMap<String, SortedMap<String, Object>> bankEntries,
    final DayAndAmount dam
  ) {
    return bankEntries.entrySet().stream()
      .filter(e->e.getValue().get("day").equals(dam.day()))
      .filter(e->e.getValue().get("amount").equals(dam.amount))
      .findAny().get().getKey()
    ;
  }

  private static String getId(
    final SortedMap<String, SortedMap<String, Object>> bankEntries,
    final BigDecimal amount
  ) {
    return bankEntries.entrySet().stream()
      .filter(e->e.getValue().get("amount").equals(amount)).findAny().get().getKey()
    ;
  }

  private Set<BigDecimal> getUniqueAmounts(
      final Collection<SortedMap<String, Object>> bankEntries,
      final Collection<SortedMap<Column, Object>> accountingEntries
  ) {
    final Set<BigDecimal> uniqueBank = unique(
        bankEntries.stream()
        .map(e->(BigDecimal)e.get("amount"))
        .collect(Collectors.toSet())
      );
      return intersection(
        uniqueBank,
        unique(
          accountingEntries.stream()
          .map(this::neutralBankAmout)
          .collect(Collectors.toList())
        )
      );
  }

  private BigDecimal neutralBankAmout(final SortedMap<Column, Object> e) {
    final Section section = (Section) e.get(Column.SECTION);
    return ((BigDecimal)e.get(Column.BANK)).multiply(section.factor());
  }

  private Set<DayAndAmount> getUniqueDams(
    final Collection<SortedMap<String, Object>> bankEntries,
    final Collection<SortedMap<Column, Object>> accountingEntries
  ) {
    final Set<DayAndAmount> uniqueBank = unique(
      bankEntries.stream()
      .map(ConvertBank::getDam)
      .collect(Collectors.toSet())
    );
    return intersection(
      uniqueBank,
      unique(
        accountingEntries.stream()
        .map(e->{
          return new DayAndAmount(
            (IsoDay)e.get(Column.DAY),
            neutralBankAmout(e)
          );
        })
        .collect(Collectors.toList())
      )
    );
  }



}
