package com.github.gv2011.tools.osm;

import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.format;
import static com.github.gv2011.util.ex.Exceptions.run;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.github.gv2011.util.AutoCloseableNt;

public class Database implements AutoCloseableNt{

  public static void main(final String[] args) throws SQLException {
    try(Connection cn = DriverManager.getConnection("jdbc:h2:file:C:/work/osm/db/h2")){
      cn.createStatement().execute("CREATE TABLE IF NOT EXISTS tag "
          + "(elementname VARCHAR, elementid BIGINT, name VARCHAR, value VARCHAR)");
//      cn.createStatement().execute("CREATE TABLE IF NOT EXISTS element "
//          + "(name VARCHAR, parent VARCHAR)");
//      cn.createStatement().execute("CREATE TABLE IF NOT EXISTS attribute "
//          + "(name VARCHAR, element VARCHAR)");
      cn.createStatement().execute(
        "MERGE INTO tag (elementname, elementid, name, value) KEY (elementname, elementid) "
        + "VALUES ('node',13,'lola','lila')");

      final ResultSet rs = cn.createStatement().executeQuery(
//        "SELECT * FROM tag"
//          "SELECT DISTINCT name FROM tag ORDER BY name"
          "SELECT name, COUNT(name) name FROM tag GROUP BY name ORDER BY 2"
//          "SELECT COUNT(*) FROM tag"
//          "SELECT COUNT(DISTINCT name) FROM tag"
      );
      while(rs.next()){
        final int colCount = rs.getMetaData().getColumnCount();
        for(int i=1; i<=colCount; i++){
          System.out.print(rs.getObject(i)+" ");
        }
        System.out.println();
      }
    }
  }

  private final Connection cn;

  public Database(final String url){
    cn = call(()->DriverManager.getConnection(url));
  }

  @Override
  public void close(){
    run(()->cn.close());
  }

  public void addTag(final Tag tag){
    run(()->{
      try(Statement st = cn.createStatement()){
        final Element parent = tag.parent().get();
        st.execute(
           format(
             "MERGE INTO tag (elementname, elementid, name, value) KEY (elementname, elementid) "
             + "VALUES ({},{},{},{})",
             escape(parent.eName()),
             parent.id().get(),
             escape(tag.name()),
             escape(tag.value())
           )
        );
      }
    });
  }
private String escape(final String str) {
  return "'"+str.replace("'", "''")+"'";
}

}
