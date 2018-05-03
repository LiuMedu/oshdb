package org.heigit.bigspatialdata.oshdb.util.celliterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.heigit.bigspatialdata.oshdb.grid.GridOSHNodes;
import org.heigit.bigspatialdata.oshdb.grid.GridOSHWays;
import org.heigit.bigspatialdata.oshdb.osh.OSHNode;
import org.heigit.bigspatialdata.oshdb.osh.OSHWay;
import org.heigit.bigspatialdata.oshdb.osm.OSMNode;
import org.heigit.bigspatialdata.oshdb.osm.OSMWay;
import org.heigit.bigspatialdata.oshdb.util.OSHDBBoundingBox;
import org.heigit.bigspatialdata.oshdb.util.celliterator.CellIterator.IterateAllEntry;
import org.heigit.bigspatialdata.oshdb.util.geometry.helpers.OSMXmlReaderTagInterpreter;
import org.heigit.bigspatialdata.oshdb.util.tagInterpreter.TagInterpreter;
import org.heigit.bigspatialdata.oshdb.util.test.OSMXmlReader;
import org.heigit.bigspatialdata.oshdb.util.time.OSHDBTimestamps;
import org.junit.Test;
import scala.collection.LinearSeq;

public class NewIterateByContributionWaysTest {
  private GridOSHWays oshdbDataGridCell;
  private GridOSHNodes oshdbDataGridCellNodes;
  private final OSMXmlReader osmXmlTestData = new OSMXmlReader();
  TagInterpreter areaDecider;

  public NewIterateByContributionWaysTest() throws IOException {
    osmXmlTestData.add("./src/test/resources/different-timestamps/way.osm");
    areaDecider = new OSMXmlReaderTagInterpreter(osmXmlTestData);
    List<OSHNode> oshNodes = new ArrayList<>();
    for (Entry<Long, Collection<OSMNode>> entry : osmXmlTestData.nodes().asMap().entrySet()) {
      oshNodes.add(OSHNode.build(new ArrayList<>(entry.getValue())));
    }
    oshdbDataGridCellNodes = GridOSHNodes.rebase(-1, -1, 0, 0, 0, 0,
        oshNodes
    );
    List<OSHWay> oshWays = new ArrayList<>();
    for (Entry<Long, Collection<OSMWay>> entry : osmXmlTestData.ways().asMap().entrySet()) {
      oshWays.add(OSHWay.build(new ArrayList<>(entry.getValue()),oshNodes));
    }
    oshdbDataGridCell = GridOSHWays.compact(-1, -1, 0, 0, 0, 0,oshWays
    );
  }

  @Test
  public void testGeometryChange() {
    // way: creation and two geometry changes, but no tag changes
    // way getting more nodes, one disappears

    List<IterateAllEntry> result = (new CellIterator(
        new OSHDBTimestamps(
            "2000-01-01T00:00:00Z",
            "2018-01-01T00:00:00Z"
        ).get(),
        new OSHDBBoundingBox(-180,-90, 180, 90),
        areaDecider,
        oshEntity -> oshEntity.getId() == 100,
        osmEntity -> true,
        false
    )).iterateByContribution(
        oshdbDataGridCell
    ).collect(Collectors.toList());

    assertEquals(3, result.size());
    assertEquals(
        EnumSet.of(ContributionType.CREATION),
        result.get(0).activities.get()
    );
    assertEquals(
        EnumSet.of(ContributionType.GEOMETRY_CHANGE),
        result.get(1).activities.get()
    );
    assertEquals(
        EnumSet.of(ContributionType.GEOMETRY_CHANGE),
        result.get(2).activities.get()
    );
    assertEquals(31, result.get(0).changeset);

    assertEquals(4, result.get(0).geometry.get().getNumPoints());
    assertEquals(8, result.get(1).geometry.get().getNumPoints());
    assertEquals(9, result.get(2).geometry.get().getNumPoints());

    assertEquals(null, result.get(0).previousGeometry.get());
    Geometry geom = result.get(0).geometry.get();
    assertTrue(geom instanceof LineString);
    Geometry geom2 = result.get(1).geometry.get();
    assertTrue(geom2 instanceof LineString);
    Geometry geom3 = result.get(2).geometry.get();
    assertTrue(geom3 instanceof LineString);

    assertNotEquals(result.get(1).geometry.get(), result.get(1).previousGeometry.get());
    assertNotEquals(result.get(2).geometry.get(), result.get(2).previousGeometry.get());
  }

  @Test
  public void testGeometryChangeOfNodeInWay() {
    // way: creation and geometry change of nodes, but no tag changes
    // way with two then 3 nodes, first two nodes changed lat lon
    List<IterateAllEntry> result = (new CellIterator(
        new OSHDBTimestamps(
            "2000-01-01T00:00:00Z",
            "2018-01-01T00:00:00Z"
        ).get(),
        new OSHDBBoundingBox(-180,-90, 180, 90),
        areaDecider,
        oshEntity -> oshEntity.getId() == 101,
        osmEntity -> true,
        false
    )).iterateByContribution(
        oshdbDataGridCell
    ).collect(Collectors.toList());
    result.iterator().forEachRemaining(k ->System.out.println(k.osmEntity.toString()));
    assertEquals(4, result.size());
    assertEquals(
        EnumSet.of(ContributionType.CREATION),
        result.get(0).activities.get()
    );
    assertEquals(
        EnumSet.of(ContributionType.GEOMETRY_CHANGE),
        result.get(1).activities.get()
    );
    assertEquals(
        EnumSet.of(ContributionType.GEOMETRY_CHANGE),
        result.get(2).activities.get()
    );
    assertEquals(
        EnumSet.of(ContributionType.GEOMETRY_CHANGE),
        result.get(3).activities.get()
    );
    System.out.println(result.get(0).osmEntity.getChangeset());
    //assertEquals(34, result.get(0).changeset);

    assertEquals(2, result.get(0).geometry.get().getNumPoints());
    assertEquals(2, result.get(1).geometry.get().getNumPoints());
    assertEquals(2, result.get(2).geometry.get().getNumPoints());
    assertEquals(3, result.get(3).geometry.get().getNumPoints());

    assertEquals(null, result.get(0).previousGeometry.get());
    Geometry geom = result.get(0).geometry.get();
    assertTrue(geom instanceof LineString);
    Geometry geom2 = result.get(1).geometry.get();
    assertTrue(geom2 instanceof LineString);
    Geometry geom3 = result.get(2).geometry.get();
    assertTrue(geom3 instanceof LineString);
    Geometry geom4 = result.get(3).geometry.get();
    assertTrue(geom4 instanceof LineString);

    assertNotEquals(result.get(1).geometry.get(), result.get(1).previousGeometry.get());
    assertNotEquals(result.get(2).geometry.get(), result.get(2).previousGeometry.get());
    assertNotEquals(result.get(3).geometry.get(), result.get(3).previousGeometry.get());
  }

  @Test
  public void testVisibleChange() {
    // way: creation and 2 visible changes, but no geometry and no tag changes
    // way visible tag changed

    List<IterateAllEntry> result = (new CellIterator(
        new OSHDBTimestamps(
            "2000-01-01T00:00:00Z",
            "2018-01-01T00:00:01Z"
        ).get(),
        new OSHDBBoundingBox(-180,-90, 180, 90),
        areaDecider,
        oshEntity -> oshEntity.getId() == 102,
        osmEntity -> true,
        false
    )).iterateByContribution(
        oshdbDataGridCell
    ).collect(Collectors.toList());
    result.iterator().forEachRemaining(k ->System.out.println(k.osmEntity.toString()));
    assertEquals(3, result.size());
    assertEquals(
        EnumSet.of(ContributionType.CREATION),
        result.get(0).activities.get()
    );
    assertEquals(
        EnumSet.of(ContributionType.DELETION),
        result.get(1).activities.get()
    );
    assertEquals(
        EnumSet.of(ContributionType.CREATION),
        result.get(2).activities.get()
    );
    System.out.println(result.get(0).osmEntity.getChangeset());
    System.out.println(result.get(0).osmEntity.toString());
    System.out.println(result.get(0).changeset);
    System.out.println(result.get(1).changeset);
    System.out.println(result.get(2).changeset);

    assertEquals(36, result.get(0).changeset);
  }

  @Test
  public void testTagChange() {
    // way: creation and two tag changes, one geometry change

    List<IterateAllEntry> result = (new CellIterator(
        new OSHDBTimestamps(
            "2000-01-01T00:00:00Z",
            "2018-01-01T00:00:01Z"
        ).get(),
        new OSHDBBoundingBox(-180,-90, 180, 90),
        areaDecider,
        oshEntity -> oshEntity.getId() == 103,
        osmEntity -> true,
        false
    )).iterateByContribution(
        oshdbDataGridCell
    ).collect(Collectors.toList());
    result.iterator().forEachRemaining(k ->System.out.println(k.osmEntity.toString()));
    assertEquals(3, result.size());
    assertEquals(
        EnumSet.of(ContributionType.CREATION),
        result.get(0).activities.get()
    );
    assertEquals(
        EnumSet.of(ContributionType.TAG_CHANGE,ContributionType.GEOMETRY_CHANGE),
        result.get(1).activities.get()
    );
    assertEquals(
        EnumSet.of(ContributionType.TAG_CHANGE),
        result.get(2).activities.get()
    );

    System.out.println(result.get(0).osmEntity.getChangeset());
    System.out.println(result.get(0).changeset);
    System.out.println(result.get(1).changeset);
    System.out.println(result.get(2).changeset);
    assertEquals(39, result.get(0).changeset);

    assertEquals(3, result.get(0).geometry.get().getNumPoints());
    assertEquals(5, result.get(1).geometry.get().getNumPoints());
    assertEquals(5, result.get(2).geometry.get().getNumPoints());


    assertEquals(null, result.get(0).previousGeometry.get());
    Geometry geom = result.get(0).geometry.get();
    assertTrue(geom instanceof LineString);
    Geometry geom2 = result.get(1).geometry.get();
    assertTrue(geom2 instanceof LineString);
    Geometry geom3 = result.get(2).geometry.get();
    assertTrue(geom3 instanceof LineString);

    assertNotEquals(result.get(1).geometry.get(), result.get(1).previousGeometry.get());
    assertEquals(result.get(2).geometry.get(), result.get(2).previousGeometry.get());
  }

  @Test
  public void testMultipleChangesOnNodesOfWay() {
    // way: nodes have different changes
    // node 12: tag change
    // node 13: visible change
    // node 14: multiple changes

    List<IterateAllEntry> result = (new CellIterator(
        new OSHDBTimestamps(
            "2000-01-01T00:00:00Z",
            "2018-01-01T00:00:01Z"
        ).get(),
        new OSHDBBoundingBox(-180,-90, 180, 90),
        areaDecider,
        oshEntity -> oshEntity.getId() == 104,
        osmEntity -> true,
        false
    )).iterateByContribution(
        oshdbDataGridCell
    ).collect(Collectors.toList());
    result.iterator().forEachRemaining(k ->System.out.println(k.osmEntity.toString()));
    // warum kriegt man alle zw schritte wo  sich node ändert?
    // 5 mal version 1: zählt node 14 version 4 nicht?
    assertEquals(6, result.size());
    assertEquals(2, result.get(0).geometry.get().getNumPoints());
    assertEquals(3, result.get(1).geometry.get().getNumPoints());
    assertEquals(2, result.get(2).geometry.get().getNumPoints());
    assertEquals(3, result.get(3).geometry.get().getNumPoints());
    assertEquals(3, result.get(4).geometry.get().getNumPoints());
    assertEquals(3, result.get(5).geometry.get().getNumPoints());

    System.out.println(result.get(3).geometry.get());
    System.out.println(result.get(4).geometry.get());
    assertEquals(
        EnumSet.of(ContributionType.CREATION),
        result.get(0).activities.get()
    );

    assertEquals(
        EnumSet.of(ContributionType.GEOMETRY_CHANGE),
        result.get(1).activities.get()
    );
    assertEquals(
        EnumSet.of(ContributionType.GEOMETRY_CHANGE),
        result.get(2).activities.get()
    );
    assertEquals(
        EnumSet.of(ContributionType.GEOMETRY_CHANGE),
        result.get(3).activities.get()
    );
    /*assertEquals(
        EnumSet.of(ContributionType.GEOMETRY_CHANGE),
        result.get(4).activities.get()
    );
    assertEquals(
        EnumSet.of(ContributionType.GEOMETRY_CHANGE),
        result.get(5).activities.get()
    );*/
   // assertEquals(42, result.get(0).changeset);
    assertNotEquals(result.get(1).osmEntity.getRawTags(), result.get(0).osmEntity.getRawTags());
    assertNotEquals(result.get(3).osmEntity.getRawTags(), result.get(1).osmEntity.getRawTags());
    assertEquals(result.get(4).osmEntity.getRawTags(), result.get(3).osmEntity.getRawTags());
    assertNotEquals(result.get(5).osmEntity.getRawTags(), result.get(4).osmEntity.getRawTags());
  }


  @Test
  public void testMultipleChanges() {
    // way and nodes have different changes

    List<IterateAllEntry> result = (new CellIterator(
        new OSHDBTimestamps(
            "2000-01-01T00:00:00Z",
            "2018-01-01T00:00:01Z"
        ).get(),
        new OSHDBBoundingBox(-180,-90, 180, 90),
        areaDecider,
        oshEntity -> oshEntity.getId() == 105,
        osmEntity -> true,
        false
    )).iterateByContribution(
        oshdbDataGridCell
    ).collect(Collectors.toList());
    result.iterator().forEachRemaining(k ->System.out.println(k.osmEntity.toString()));
    assertEquals(6, result.size());
    assertEquals(
        EnumSet.of(ContributionType.CREATION),
        result.get(0).activities.get()
    );
    assertEquals(
        EnumSet.of(ContributionType.TAG_CHANGE,ContributionType.GEOMETRY_CHANGE),
        result.get(1).activities.get()
    );
    assertEquals(
        EnumSet.of(ContributionType.GEOMETRY_CHANGE),
        result.get(2).activities.get()
    );
    assertEquals(
        EnumSet.of(ContributionType.GEOMETRY_CHANGE),
        result.get(3).activities.get()
    );
    assertEquals(
        EnumSet.of(ContributionType.DELETION),
        result.get(4).activities.get()
    );
    assertEquals(
        EnumSet.of(ContributionType.CREATION),
        result.get(5).activities.get()
    );

    assertEquals(44, result.get(0).changeset);
    assertNotEquals(result.get(1).osmEntity.getRawTags(), result.get(0).osmEntity.getRawTags());
    assertEquals(result.get(5).osmEntity.getRawTags(), result.get(3).osmEntity.getRawTags());

  }

  @Test
  public void testPolygonAreaYesTagDisappears() {
    // way seems to be polygon with area=yes, later linestring because area=yes deleted
    List<IterateAllEntry> result = (new CellIterator(
        new OSHDBTimestamps(
            "2000-01-01T00:00:00Z",
            "2018-01-01T00:00:01Z"
        ).get(),
        new OSHDBBoundingBox(-180,-90, 180, 90),
        areaDecider,
        oshEntity -> oshEntity.getId() == 106,
        osmEntity -> true,
        false
    )).iterateByContribution(
        oshdbDataGridCell
    ).collect(Collectors.toList());
    assertEquals(2, result.size());
    assertEquals(
        EnumSet.of(ContributionType.CREATION),
        result.get(0).activities.get()
    );
    // auch geometrychange wenn area=yes wegfällt?
    assertEquals(
        EnumSet.of(ContributionType.TAG_CHANGE,ContributionType.GEOMETRY_CHANGE),
        result.get(1).activities.get()
    );

    assertEquals(48, result.get(0).changeset);

    assertEquals(5, result.get(0).geometry.get().getNumPoints());
    assertEquals(5, result.get(1).geometry.get().getNumPoints());

    assertEquals(null, result.get(0).previousGeometry.get());
    Geometry geom = result.get(0).geometry.get();
    assertTrue(geom instanceof Polygon);
    Geometry geom2 = result.get(1).geometry.get();
    assertTrue(geom2 instanceof LineString);

    assertNotEquals(result.get(1).geometry.get(), result.get(1).previousGeometry.get());
  }

  @Test
  public void testPolygonAreaYesNodeDisappears() {
    // way seems to be polygon with area=yes, later linestring because one node deleted
    List<IterateAllEntry> result = (new CellIterator(
        new OSHDBTimestamps(
            "2000-01-01T00:00:00Z",
            "2018-01-01T00:00:01Z"
        ).get(),
        new OSHDBBoundingBox(-180,-90, 180, 90),
        areaDecider,
        oshEntity -> oshEntity.getId() == 107,
        osmEntity -> true,
        false
    )).iterateByContribution(
        oshdbDataGridCell
    ).collect(Collectors.toList());
    assertEquals(2, result.size());
    assertEquals(
        EnumSet.of(ContributionType.CREATION),
        result.get(0).activities.get()
    );
    assertEquals(
        EnumSet.of(ContributionType.GEOMETRY_CHANGE),
        result.get(1).activities.get()
    );

    assertEquals(50, result.get(0).changeset);

    assertEquals(5, result.get(0).geometry.get().getNumPoints());
    assertEquals(4, result.get(1).geometry.get().getNumPoints());

    assertEquals(null, result.get(0).previousGeometry.get());
    Geometry geom = result.get(0).geometry.get();
    assertTrue(geom instanceof Polygon);
    Geometry geom2 = result.get(1).geometry.get();
    assertTrue(geom2 instanceof LineString);

    assertNotEquals(result.get(1).geometry.get(), result.get(1).previousGeometry.get());
  }
}
