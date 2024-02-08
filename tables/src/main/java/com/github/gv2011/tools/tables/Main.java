package com.github.gv2011.tools.tables;

import static com.github.gv2011.util.CollectionUtils.toSingle;
import static com.github.gv2011.util.ex.Exceptions.callWithCloseable;
import static com.github.gv2011.util.icol.ICollections.iCollections;
import static com.github.gv2011.util.icol.ICollections.listBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.github.gv2011.util.FileUtils;
import com.github.gv2011.util.StringUtils;
import com.github.gv2011.util.icol.IList;
import com.github.gv2011.util.icol.IList.Builder;
import com.github.gv2011.util.icol.IMap;
import com.github.gv2011.util.num.NumUtils;
import com.github.gv2011.util.time.TimeUtils;
import com.github.miachm.sods.OfficeCurrency;
import com.github.miachm.sods.Range;
import com.github.miachm.sods.Sheet;
import com.github.miachm.sods.SpreadSheet;

public class Main {

	public static void main(final String[] args) throws IOException {
		final Path file = Files.list(FileUtils.WORK_DIR.resolve("work"))
			.filter(p->p.getFileName().toString().endsWith(".csv"))
			.collect(toSingle())
		;
		final Path outFile = file.getParent().resolve(
	    StringUtils.removeTail(file.getFileName().toString(), ".csv") + ".ods"
    );
		System.out.println(file);
	    final Builder<Map<String, Object>> result = listBuilder();

      final Map<String, Integer> headers = callWithCloseable(
	      ()->FileUtils.getReaderRemoveBom(file),
	      r->{
	        final CSVFormat format =
	          CSVFormat.RFC4180.builder()
	          .setDelimiter(';')
	          .setQuote('"')
	          .setHeader()
	          .setSkipHeaderRecord(true)
	          .setRecordSeparator("\r\n")
	          .build()
	        ;
	        try(CSVParser p = new CSVParser(r, format)){
	          for(final CSVRecord record: p){
	            final IMap<String, String> map = iCollections().mapFrom(record.toMap());
	            IMap<String, Object> converted = convert(map);
              if(filter(converted)) result.add(converted);
	          }
	          return p.getHeaderMap();
	        }
	      }
	    );
	    final IList<Map<String, Object>> map = result.build();
      map.forEach(System.out::println);
	    final SpreadSheet doc = new SpreadSheet();
	    final Sheet sheet = new Sheet("1");
	    headers.forEach((h,i)->sheet.appendColumn());
	    sheet.appendRow();
	    headers.forEach((h,i)->sheet.getRange(0, i).setValue(h));
	    IntStream.range(0, map.size()).forEach(j->{
	      sheet.appendRow();
	      Map<String, Object> row = map.get(j);
	      headers.forEach((h,i)->{
          Range range = sheet.getRange(j+1, i);
          range.setValue(row.get(h));
        });
	    });
      doc.appendSheet(sheet);
      callWithCloseable(()->Files.newOutputStream(outFile), os->{doc.save(os);});
	}

  private static boolean filter(final IMap<String, Object> row) {
    return row.get("Empfänger/Absender").equals("Mathias Stollberg");
  }

  private static IMap<String, Object> convert(final IMap<String, String> map) {
    return map.entrySet().stream()
      .collect(iCollections().mapCollector(
        Entry::getKey,
        e->convert(e)
      ))
    ;
  }

  private static Object convert(final Entry<String, String> cell) {
    if(cell.getKey().equals("Buchung")){
      return LocalDate.parse(cell.getValue(), TimeUtils.DIN_1355_1_DATE).toString();
    }
    else if(cell.getKey().equals("Betrag")){
      return new OfficeCurrency(
        Currency.getInstance("EUR"),
        NumUtils.parseComma(StringUtils.removeTail(cell.getValue(), " EUR")).doubleValue()
      );
    }
    else return cell.getValue().trim();
  }

}
