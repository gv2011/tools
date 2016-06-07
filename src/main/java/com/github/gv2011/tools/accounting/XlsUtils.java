package com.github.gv2011.tools.accounting;

import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.run;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.WorkbookUtil;

import com.github.gv2011.util.AutoCloseableNt;
import com.github.gv2011.util.ann.Nullable;

public class XlsUtils {

  public static XlsSheet createEmpty(final Path writeFile){
    return new XlsSheet(writeFile);
  }

  public static XlsSheet read(final Path readFile){
    return call(()->{
      try(InputStream in = Files.newInputStream(readFile)){
        return new XlsSheet(in, Optional.empty());
      }
    });
  }

  public static XlsSheet open(final Path readFile, final Path writeFile){
    return call(()->{
      try(InputStream in = Files.newInputStream(readFile)){
        return new XlsSheet(in, Optional.of(writeFile));
      }
    });
  }

  public static class XlsSheet implements Supplier<HSSFSheet>, AutoCloseableNt{

    private final HSSFSheet sheet;
//    private final POIFSFileSystem fs;
    private final HSSFWorkbook workbook;
    private final Optional<Path> writeFile;

    private XlsSheet(final Path writeFile){
      this.writeFile = Optional.of(writeFile);
      workbook = new HSSFWorkbook();
      sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName("sheet1"));
    }

    private XlsSheet(final InputStream in, final Optional<Path> writeFile) throws IOException {
      this.writeFile = writeFile;
      workbook = new HSSFWorkbook(in);
      verify(workbook.getNumberOfSheets()==1);
      sheet = workbook.getSheetAt(0);
    }

    @Override
    public void close() {
      run(()->{
        if(writeFile.isPresent()){
          try(final OutputStream os = Files.newOutputStream(
              writeFile.get(),
              StandardOpenOption.CREATE,
              StandardOpenOption.TRUNCATE_EXISTING
          )){
            workbook.write(os);
          }
        }
        workbook.close();
      });
    }

    @Override
    public HSSFSheet get() {
      return sheet;
    }

  }

  public static BigDecimal readNumber(final @Nullable HSSFCell cell) {
    final BigDecimal result;
    if(cell==null) result = BigDecimal.ZERO;
    else{
      final long rounded = Math.round(cell.getNumericCellValue()*100d);
      result = BigDecimal.valueOf(rounded).divide(BigDecimal.valueOf(100l));
      if(result.scale()>2) throw new RuntimeException(result+ " " + cell);
    }
    return result;
  }

}
