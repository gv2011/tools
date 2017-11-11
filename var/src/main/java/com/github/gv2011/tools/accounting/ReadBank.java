package com.github.gv2011.tools.accounting;

import static com.github.gv2011.util.FileUtils.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

import com.github.gv2011.tools.accounting.XlsUtils.XlsSheet;
import com.github.gv2011.util.IsoDay;

public class ReadBank {

  public static void main(final String[] args) throws IOException {
    BigDecimal sum = BigDecimal.ZERO;
    for(final Entry<String, SortedMap<String,Object>> e: new ReadBank().read().entrySet()){
      final DayAndAmount dam = ConvertBank.getDam(e.getValue());
      sum = sum.add(dam.amount());
      System.out.println(e.getKey() + ":" + dam);
    }
    System.out.println("Sum:" + sum);
  }

  public SortedMap<String, SortedMap<String,Object>> read(){
    try(XlsSheet xs = XlsUtils.read(path("data","bank.xls"))){
      final HSSFSheet sheet = xs.get();
      final int rows = sheet.getPhysicalNumberOfRows();
      final SortedMap<String, SortedMap<String,Object>> entries = new TreeMap<>();
      for(int r = 0; r < rows; r++) {
        final HSSFRow row = sheet.getRow(r);
        final String key = row.getCell(0).getStringCellValue().trim();
        final IsoDay day = new IsoDay(row.getCell(1).getStringCellValue().trim());
        final BigDecimal amount = XlsUtils.readNumber(row.getCell(2));
        final String peer = row.getCell(3).getStringCellValue().trim();
        final String subject = row.getCell(4).getStringCellValue().trim();
        final SortedMap<String, Object> entry = new TreeMap<>();
        entry.put("key", key);
        entry.put("day", day);
        entry.put("amount", amount);
        entry.put("peer", peer);
        entry.put("subject", subject);
        entries.put(key, entry);
      }
      return entries;
    }
  }

}
