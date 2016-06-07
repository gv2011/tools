package com.github.gv2011.tools.accounting;

import static com.github.gv2011.util.StringUtils.alignRight;
import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.run;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;

import com.github.gv2011.util.AutoCloseableNt;
import com.github.gv2011.util.IsoDay;

public class ConvertBank implements AutoCloseableNt{

  public static void main(final String[] args) throws IOException {
    try(ConvertBank convertBank = new ConvertBank()){
      convertBank.convert();
    };
  }

  private int rowCount;

  private void convert() {
    final BigDecimal balance = new BigDecimal("-229.58");
    final List<Map<String,Object>> list = readList();
    final SortedMap<DayAndAmount,Set<Integer>> sorted = new TreeMap<>();
    for(int i=0; i<list.size(); i++){
      sorted.computeIfAbsent(getDam(list.get(i)), d->new HashSet<>()).add(i);
    }
    final SortedMap<String,Integer> byKey = new TreeMap<>();
    for(final Entry<DayAndAmount,Set<Integer>> e: sorted.entrySet()){
      for(final int i: e.getValue()){
        final DayAndAmount dam = getDam(list.get(i));
        final String key = getKey(byKey.keySet(), dam.day);
        byKey.put(key, i);
      }
    }
    verify(byKey.size()==list.size());
    BigDecimal sum = BigDecimal.ZERO;
    for(final Entry<String,Integer> e: byKey.entrySet()){
      final Map<String, Object> entry = list.get(e.getValue());
      final DayAndAmount dam = getDam(entry);
      System.out.println(e.getKey() + ":" + dam);
      addRecord(e.getKey(), entry);
      sum = sum.add(dam.amount);
    }
    System.out.println("Start balance: "+balance);
    System.out.println("Sum:           "+sum);
    System.out.println("End balance:   "+balance.add(sum));
    System.out.println("Entries:       "+list.size());
    System.out.println("Inconclusive:  "+(list.size()-(new HashSet<>(list).size())));
  }

  static DayAndAmount getDam(final Map<String, Object> entry) {
    return new DayAndAmount((IsoDay)entry.get("day"), (BigDecimal)entry.get("amount"));
  }

  private List<Map<String,Object>> readList() {
    return call(()->{
      try(final Reader in = new FileReader("data/bank.txt")){
        final CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(';');
        final Iterable<CSVRecord> records = format.parse(in);
        final List<Map<String,Object>> list = new ArrayList<>();
        for(final CSVRecord r: records){
          final IsoDay day = IsoDay.fromDdMmYyyy(r.get("Buchung"));
          final String b = r.get("Betrag");
          final BigDecimal amount = toNumber(b);
          final Map<String,Object> entry = new HashMap<>();
          entry.put("day", day);
          entry.put("amount", amount);
          entry.put("peer", r.get("Empfänger/Absender").trim());
          entry.put("subject", nice(r.get("Verwendungszweck")));
          list.add(entry);
          ;
        }
        return list;
      }
    });
  }

  private String nice(final String subject) {
    final StringBuilder sb = new StringBuilder();
    boolean start = false;
    for(String line: Arrays.asList(subject.split(Pattern.quote("<BR>")))){
      line = line.trim();
      if(line.startsWith("SVWZ+")) {
        line = line.substring(5).trim(); start = true;
      }
      if(start && !line.isEmpty()){
        if(sb.length()>0) sb.append(' ');
        sb.append(line);
      }
    }
    final String result = sb.toString();
    return result.isEmpty()?nice1(subject):result;
  }

  private String nice1(final String subject) {
    return Arrays.asList(subject.split(Pattern.quote("<BR>"))).stream()
    .map(String::trim)
    .filter(s->!s.isEmpty())
    .filter(s->!s.equals("EREF+NOTPROVIDED"))
    .collect(Collectors.joining(" "));
  }

  private String getKey(final Set<String> entries, final IsoDay day) {
    boolean found = false;
    int i=1;
    String key = null;
    while(!found){
      key = getKey(day, i);
      if(!entries.contains(key)) found = true;
      else i++;
    }
    return key;
  }

  private String getKey(final IsoDay day, final int i) {
    return day+"."+alignRight(Integer.toString(i),2,'0');
  }

  private void addRecord(final String key, final Map<String, Object> entry) {
    final Row row = sheet.createRow(rowCount++);
    row.createCell(0, Cell.CELL_TYPE_STRING).setCellValue(key);
    row.createCell(1, Cell.CELL_TYPE_STRING).setCellValue(entry.get("day").toString());
    row.createCell(2, Cell.CELL_TYPE_NUMERIC).setCellValue(((BigDecimal)entry.get("amount")).doubleValue());
    row.createCell(3, Cell.CELL_TYPE_STRING).setCellValue(entry.get("peer").toString());
    row.createCell(4, Cell.CELL_TYPE_STRING).setCellValue(entry.get("subject").toString());
  }

  private final Workbook workbook;
  private final Sheet sheet;

  public ConvertBank() throws IOException {
     workbook = new HSSFWorkbook();
     sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName("sheet1"));
    }

  @Override
  public void close() {
    run(()->{
      try{
        try(final FileOutputStream fileOut = new FileOutputStream("data/bank.xls")){
          workbook.write(fileOut);
        }
      }finally{
        workbook.close();
      }
    });
  }

  private static BigDecimal toNumber(final String amount) {
    return new BigDecimal(amount.replaceAll("[. EUR]+", "").replace(',', '.'));
  }

}
