package org.heigit.bigspatialdata.oshdb.api.mapper;

import com.vividsolutions.jts.geom.Geometry;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import org.heigit.bigspatialdata.oshdb.OSHDB;
import org.heigit.bigspatialdata.oshdb.OSHDB_H2;
import org.heigit.bigspatialdata.oshdb.api.generic.TriFunction;
import org.heigit.bigspatialdata.oshdb.grid.GridOSHEntity;
import org.heigit.bigspatialdata.oshdb.osm.OSMEntity;
import org.heigit.bigspatialdata.oshdb.util.BoundingBox;
import org.heigit.bigspatialdata.oshdb.util.CellId;
import org.heigit.bigspatialdata.oshdb.util.CellIterator;
import org.heigit.bigspatialdata.oshdb.util.tagInterpreter.DefaultTagInterpreter;
import org.heigit.bigspatialdata.oshdb.util.tagInterpreter.TagInterpreter;
import org.heigit.bigspatialdata.oshdb.api.objects.Timestamp;
import org.json.simple.parser.ParseException;

public class Mapper_H2 extends Mapper {
  
  protected Mapper_H2(OSHDB oshdb) {
    super(oshdb);
  }
  
  @Override
  protected <R, S> S reduceCells(Iterable<CellId> cellIds, List<Long> tstampsIds, BoundingBox bbox, Predicate<OSMEntity> filter, TriFunction<Timestamp, Geometry, OSMEntity, R> f, S s, BiFunction<S, R, S> rf) throws SQLException, IOException, ParseException, ClassNotFoundException {
    //load tag interpreter helper which is later used for geometry building
    final TagInterpreter tagInterpreter = DefaultTagInterpreter.fromH2(((OSHDB_H2) this._oshdb).getConnection());
    
    for (CellId cellId : cellIds) {
      // prepare SQL statement
      PreparedStatement pstmt = ((OSHDB_H2) this._oshdb).getConnection().prepareStatement("(select data from grid_node where level = ?1 and id = ?2) union (select data from grid_way where level = ?1 and id = ?2) union (select data from grid_relation where level = ?1 and id = ?2)");
      pstmt.setInt(1, cellId.getZoomLevel());
      pstmt.setLong(2, cellId.getId());
      
      // execute statement
      ResultSet oshCellsRawData = pstmt.executeQuery();
      
      // iterate over the result
      while (oshCellsRawData.next()) {
        // get one cell from the raw data stream
        GridOSHEntity oshCellRawData = (GridOSHEntity) (new ObjectInputStream(oshCellsRawData.getBinaryStream(1))).readObject();

        // iterate over the history of all OSM objects in the current cell
        List<R> rs = new ArrayList<>();
        CellIterator.iterateByTimestamps(oshCellRawData, bbox, tstampsIds, tagInterpreter, filter, false).forEach(result -> result.entrySet().forEach(entry -> {
          List<Long> x = tstampsIds;
          Timestamp tstamp = new Timestamp(entry.getKey());
          Geometry geometry = entry.getValue().getRight();
          OSMEntity entity = entry.getValue().getLeft();
          rs.add(f.apply(tstamp, geometry, entity));
        }));
        
        // fold the results
        for (R r : rs) {
          s = rf.apply(s, r);
        }
      }
    }
    return s;
  }   
}
