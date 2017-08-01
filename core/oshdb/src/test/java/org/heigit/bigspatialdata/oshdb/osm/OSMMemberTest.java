/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.heigit.bigspatialdata.oshdb.osm;

import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;

import org.heigit.bigspatialdata.oshdb.osm.OSMMember;
import org.heigit.bigspatialdata.oshdb.util.OSMType;
import org.junit.Test;

/**
 *
 * @author Moritz Schott <m.schott@stud.uni-heidelberg.de>
 */
public class OSMMemberTest {
  private static final Logger LOG = Logger.getLogger(OSMMemberTest.class.getName());
  
  public OSMMemberTest() {
  }

  @Test
  public void testGetId() {
    System.out.println("getId");
    OSMMember instance = new OSMMember(1L, OSMType.WAY, 1);
    long expResult = 1L;
    long result = instance.getId();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetType() {
    System.out.println("getType");
    OSMMember instance = new OSMMember(1L, OSMType.WAY, 1);
    OSMType expResult = OSMType.WAY;
    OSMType result = instance.getType();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetRoleId() {
    System.out.println("getRoleId");
    OSMMember instance = new OSMMember(1L, OSMType.WAY, 1);
    int expResult = 1;
    int result = instance.getRoleId();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetData() {
    System.out.println("getEntity (explicit null)");
    OSMMember instance = new OSMMember(1L, OSMType.WAY, 1, null);
    Object expResult = null;
    Object result = instance.getEntity();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetDataII() {
    System.out.println("getEntity (implicit null)");
    OSMMember instance = new OSMMember(1L, OSMType.WAY, 1);
    Object expResult = null;
    Object result = instance.getEntity();
    assertEquals(expResult, result);
  }


  @Test
  public void testToString() {
    System.out.println("toString");
    OSMMember instance = new OSMMember(1L, OSMType.WAY, 1);
    String expResult = "T:1 ID:1 R:1";
    String result = instance.toString();
    assertEquals(expResult, result);
  }
  
}
