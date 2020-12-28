package com.github.gv2011.tools.tables;

import static com.github.gv2011.util.CollectionUtils.toSingle;
import static com.github.gv2011.util.ex.Exceptions.callWithCloseable;
import static com.github.gv2011.util.icol.ICollections.listBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.github.gv2011.util.FileUtils;
import com.github.gv2011.util.icol.IList.Builder;

public class Main {

	public static void main(String[] args) throws IOException {
		final Path file = Files.list(FileUtils.WORK_DIR.resolve("work"))
			.filter(p->p.getFileName().toString().endsWith(".csv"))
			.collect(toSingle())
		;
		System.out.println(file);
	    final Builder<Map<String, String>> result = listBuilder();
	    
	    callWithCloseable(
	      ()->FileUtils.getReaderRemoveBom(file),
	      r->{
	        final CSVFormat format =
	          CSVFormat.RFC4180.withDelimiter(';')
	          .withQuote('"')
	          .withFirstRecordAsHeader()
	          .withRecordSeparator("\r\n")
	        ;
	        try(CSVParser p = new CSVParser(r, format)){
	          for(final CSVRecord record: p){
	            final Map<String, String> map = record.toMap();
	            result.add(map);
	          }
	        }
	      }
	    );
	    result.build().forEach(System.out::println);
	}

}
