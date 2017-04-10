package org.heigit.bigspatialdata.hosmdb.osm;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.Serializable;
import java.util.Arrays;


public class OSMWay extends OSMEntity implements Comparable<OSMWay>, Serializable {

  private static final long serialVersionUID = 1L;
  private final OSMMember[] refs;

  public OSMWay(final long id, final int version, final long timestamp, final long changeset,
      final int userId, final int[] tags, final OSMMember[] refs) {
    super(id, version, timestamp, changeset, userId, tags);
    this.refs = refs;
  }


  public OSMMember[] getRefs() {
    return refs;
  }
  
  @Override
  public String toString() {
    return String.format("WAY-> %s Refs:%s", super.toString(), Arrays.toString(getRefs()));
  }


  @Override
  public int compareTo(OSMWay o) {
    int c = Long.compare(id, o.id);
    if (c == 0)
      c = Integer.compare(Math.abs(version), Math.abs(o.version));
    if (c == 0)
      c = Long.compare(timestamp, o.timestamp);
    return c;
  }


  @Override
  public boolean isAuxiliary() {
    throw new NotImplementedException();
    // todo: return true if no own tags and member of a relation (e.g. multipolygon)
  }
  @Override
  public boolean isPoint() {
    return false;
  }
  @Override
  public boolean isPointLike() {
    return this.isArea();
  }
  @Override
  public boolean isArea() {
    throw new NotImplementedException();
    // todo: get polygon_features.json + key/value table -> hashmap of areatags -> …
  }
  @Override
  public boolean isLine() {
    OSMMember[] nds = this.getRefs();
    if (nds[0].getId() != nds[nds.length-1].getId())
      return true;
    throw new NotImplementedException();
    // todo: return !this.isArea();
  }
}
