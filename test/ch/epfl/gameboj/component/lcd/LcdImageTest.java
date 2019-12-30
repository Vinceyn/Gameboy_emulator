package ch.epfl.gameboj.component.lcd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;


import ch.epfl.gameboj.bits.BitVector;

public class LcdImageTest {
    public static BitVector v0 = new BitVector(32, false);
    public static BitVector v1 = new BitVector(32, true);
    // 11001100000000001010101011110000
    public static BitVector v3 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
    // 00000000110101011111111000000000
    public static BitVector v4 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
    public static BitVector v5 = new BitVector(64);
    
    // TESTS CONSTRUCTEURS
    // Cas Normal
    @org.junit.jupiter.api.Test
    public void constructeurNormalTest() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1 = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2 = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3 = new LcdImageLine(v0, v1, v0);
        LcdImageLine l4 = new LcdImageLine(v3, v3, v3);
        LcdImageLine l5 = new LcdImageLine(v4, v4, v4);
        LcdImageLine l6 = new LcdImageLine(v3, v4, v3);
        LcdImageLine l7 = new LcdImageLine(v4, v3, v4);
        
        List<LcdImageLine> list0 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list0.add(l0);
        }
        List<LcdImageLine> list1 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list1.add(l1);
        }
        List<LcdImageLine> list2 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list2.add(l2);
        }
        List<LcdImageLine> list3 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list3.add(l3);
        }
        List<LcdImageLine> list4 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list4.add(l4);
        }
        List<LcdImageLine> list5 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list5.add(l5);
        }
        List<LcdImageLine> list6 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list6.add(l6);
        }
        List<LcdImageLine> list7 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list7.add(l7);
        }
        
        LcdImage i0 = new LcdImage(54, 32, list0);
        LcdImage i1 = new LcdImage(54, 32, list1);
        LcdImage i2 = new LcdImage(54, 32, list2);
        LcdImage i3 = new LcdImage(54, 32, list3);
        LcdImage i4 = new LcdImage(54, 32, list4);
        LcdImage i5 = new LcdImage(54, 32, list5);
        LcdImage i6 = new LcdImage(54, 32, list6);
        LcdImage i7 = new LcdImage(54, 32, list7);
    }
    
    // Cas d'erreur
    @org.junit.jupiter.api.Test
    public void constructeurErrorTest() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        
        List<LcdImageLine> list0 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list0.add(l0);
        }
        
        assertThrows(IllegalArgumentException.class, () -> {LcdImage i0 = new LcdImage(-1, -1, list0);});
        assertThrows(IllegalArgumentException.class, () -> {LcdImage i0 = new LcdImage(-1, 32, list0);});
        assertThrows(IllegalArgumentException.class, () -> {LcdImage i0 = new LcdImage(54, -1, list0);});
        assertThrows(IllegalArgumentException.class, () -> {LcdImage i0 = new LcdImage(0, 32, list0);});
        assertThrows(IllegalArgumentException.class, () -> {LcdImage i0 = new LcdImage(54, 0, list0);});
        assertThrows(IllegalArgumentException.class, () -> {LcdImage i0 = new LcdImage(43, 32, list0);});
        assertThrows(IllegalArgumentException.class, () -> {LcdImage i0 = new LcdImage(98, 32, list0);});
        assertThrows(IllegalArgumentException.class, () -> {LcdImage i0 = new LcdImage(54, 42, list0);});
        assertThrows(IllegalArgumentException.class, () -> {LcdImage i0 = new LcdImage(54, 23, list0);});
    }
    
    
    // TESTS HEIGHT / WIDTH
    @org.junit.jupiter.api.Test
    public void heightWidthTest() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1 = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2 = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3 = new LcdImageLine(v0, v1, v0);
        LcdImageLine l4 = new LcdImageLine(v3, v3, v3);
        LcdImageLine l5 = new LcdImageLine(v4, v4, v4);
        LcdImageLine l6 = new LcdImageLine(v3, v4, v3);
        LcdImageLine l7 = new LcdImageLine(v4, v3, v4);
        
        List<LcdImageLine> list0 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list0.add(l0);
        }
        List<LcdImageLine> list1 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list1.add(l1);
        }
        List<LcdImageLine> list2 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list2.add(l2);
        }
        List<LcdImageLine> list3 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list3.add(l3);
        }
        List<LcdImageLine> list4 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list4.add(l4);
        }
        List<LcdImageLine> list5 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list5.add(l5);
        }
        List<LcdImageLine> list6 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list6.add(l6);
        }
        List<LcdImageLine> list7 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list7.add(l7);
        }
        
        LcdImage i0 = new LcdImage(32, 54, list0);
        LcdImage i1 = new LcdImage(32, 54, list1);
        LcdImage i2 = new LcdImage(32, 54, list2);
        LcdImage i3 = new LcdImage(32, 54, list3);
        LcdImage i4 = new LcdImage(32, 54, list4);
        LcdImage i5 = new LcdImage(32, 54, list5);
        LcdImage i6 = new LcdImage(32, 54, list6);
        LcdImage i7 = new LcdImage(32, 54, list7);
        
        assertEquals(32, i0.width());
        assertEquals(32, i1.width());
        assertEquals(32, i2.width());
        assertEquals(32, i3.width());
        assertEquals(32, i4.width());
        assertEquals(32, i5.width());
        assertEquals(32, i6.width());
        assertEquals(32, i7.width());
        
        assertEquals(54, i0.height());
        assertEquals(54, i1.height());
        assertEquals(54, i2.height());
        assertEquals(54, i3.height());
        assertEquals(54, i4.height());
        assertEquals(54, i5.height());
        assertEquals(54, i6.height());
        assertEquals(54, i7.height());
    }
    
    
    // TESTS EQUALS
    @org.junit.jupiter.api.Test
    public void equalsTest() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1 = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2 = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3 = new LcdImageLine(v0, v1, v0);
        LcdImageLine l4 = new LcdImageLine(v3, v3, v3);
        LcdImageLine l5 = new LcdImageLine(v4, v4, v4);
        LcdImageLine l6 = new LcdImageLine(v3, v4, v3);
        LcdImageLine l7 = new LcdImageLine(v4, v3, v4);
        
        List<LcdImageLine> list0 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list0.add(l0);
        }
        List<LcdImageLine> list1 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list1.add(l1);
        }
        List<LcdImageLine> list2 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list2.add(l2);
        }
        List<LcdImageLine> list3 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list3.add(l3);
        }
        List<LcdImageLine> list4 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list4.add(l4);
        }
        List<LcdImageLine> list5 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list5.add(l5);
        }
        List<LcdImageLine> list6 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list6.add(l6);
        }
        List<LcdImageLine> list7 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list7.add(l7);
        }
        
        LcdImage i0 = new LcdImage(54, 32, list0);
        LcdImage i1 = new LcdImage(54, 32, list1);
        LcdImage i2 = new LcdImage(54, 32, list2);
        LcdImage i3 = new LcdImage(54, 32, list3);
        LcdImage i4 = new LcdImage(54, 32, list4);
        LcdImage i5 = new LcdImage(54, 32, list5);
        LcdImage i6 = new LcdImage(54, 32, list6);
        LcdImage i7 = new LcdImage(54, 32, list7);
        
        LcdImage i0a = new LcdImage(54, 32, list0);
        LcdImage i1a = new LcdImage(54, 32, list1);
        LcdImage i2a = new LcdImage(54, 32, list2);
        LcdImage i3a = new LcdImage(54, 32, list3);
        LcdImage i4a = new LcdImage(54, 32, list4);
        LcdImage i5a = new LcdImage(54, 32, list5);
        LcdImage i6a = new LcdImage(54, 32, list6);
        LcdImage i7a = new LcdImage(54, 32, list7);
        
        assertTrue(i0.equals(i0));
        assertTrue(i1.equals(i1));
        assertTrue(i2.equals(i2));
        assertTrue(i3.equals(i3));
        assertTrue(i4.equals(i4));
        assertTrue(i5.equals(i5));
        assertTrue(i6.equals(i6));
        assertTrue(i7.equals(i7));
        
        assertTrue(i0.equals(i0a));
        assertTrue(i1.equals(i1a));
        assertTrue(i2.equals(i2a));
        assertTrue(i3.equals(i3a));
        assertTrue(i4.equals(i4a));
        assertTrue(i5.equals(i5a));
        assertTrue(i6.equals(i6a));
        assertTrue(i7.equals(i7a));
        
        assertFalse(i0.equals(i7));
        assertFalse(i1.equals(i6));
        assertFalse(i2.equals(i5));
        assertFalse(i3.equals(i4));
        assertFalse(i4.equals(i3));
        assertFalse(i5.equals(i2));
        assertFalse(i6.equals(i1));
        assertFalse(i7.equals(i0));
    }
    
    
    // TESTS HASHCODE
    @org.junit.jupiter.api.Test
    public void hashCodeTest() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1 = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2 = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3 = new LcdImageLine(v0, v1, v0);
        LcdImageLine l4 = new LcdImageLine(v3, v3, v3);
        LcdImageLine l5 = new LcdImageLine(v4, v4, v4);
        LcdImageLine l6 = new LcdImageLine(v3, v4, v3);
        LcdImageLine l7 = new LcdImageLine(v4, v3, v4);
        
        List<LcdImageLine> list0 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list0.add(l0);
        }
        List<LcdImageLine> list1 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list1.add(l1);
        }
        List<LcdImageLine> list2 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list2.add(l2);
        }
        List<LcdImageLine> list3 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list3.add(l3);
        }
        List<LcdImageLine> list4 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list4.add(l4);
        }
        List<LcdImageLine> list5 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list5.add(l5);
        }
        List<LcdImageLine> list6 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list6.add(l6);
        }
        List<LcdImageLine> list7 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list7.add(l7);
        }
        
        LcdImage i0 = new LcdImage(54, 32, list0);
        LcdImage i1 = new LcdImage(54, 32, list1);
        LcdImage i2 = new LcdImage(54, 32, list2);
        LcdImage i3 = new LcdImage(54, 32, list3);
        LcdImage i4 = new LcdImage(54, 32, list4);
        LcdImage i5 = new LcdImage(54, 32, list5);
        LcdImage i6 = new LcdImage(54, 32, list6);
        LcdImage i7 = new LcdImage(54, 32, list7);
        
        LcdImage i0a = new LcdImage(54, 32, list0);
        LcdImage i1a = new LcdImage(54, 32, list1);
        LcdImage i2a = new LcdImage(54, 32, list2);
        LcdImage i3a = new LcdImage(54, 32, list3);
        LcdImage i4a = new LcdImage(54, 32, list4);
        LcdImage i5a = new LcdImage(54, 32, list5);
        LcdImage i6a = new LcdImage(54, 32, list6);
        LcdImage i7a = new LcdImage(54, 32, list7);
        
        assertEquals(i0.hashCode(), i0.hashCode());
        assertEquals(i1.hashCode(), i1.hashCode());
        assertEquals(i2.hashCode(), i2.hashCode());
        assertEquals(i3.hashCode(), i3.hashCode());
        assertEquals(i4.hashCode(), i4.hashCode());
        assertEquals(i5.hashCode(), i5.hashCode());
        assertEquals(i6.hashCode(), i6.hashCode());
        assertEquals(i7.hashCode(), i7.hashCode());
        
        assertEquals(i0.hashCode(), i0a.hashCode());
        assertEquals(i1.hashCode(), i1a.hashCode());
        assertEquals(i2.hashCode(), i2a.hashCode());
        assertEquals(i3.hashCode(), i3a.hashCode());
        assertEquals(i4.hashCode(), i4a.hashCode());
        assertEquals(i5.hashCode(), i5a.hashCode());
        assertEquals(i6.hashCode(), i6a.hashCode());
        assertEquals(i7.hashCode(), i7a.hashCode());
    }
    
    
    // TESTS BUILDER
    // Cas normal
    @org.junit.jupiter.api.Test
    public void builderNormal() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1 = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2 = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3 = new LcdImageLine(v0, v1, v0);
        LcdImageLine l4 = new LcdImageLine(v3, v3, v3);
        LcdImageLine l5 = new LcdImageLine(v4, v4, v4);
        LcdImageLine l6 = new LcdImageLine(v3, v4, v3);
        LcdImageLine l7 = new LcdImageLine(v4, v3, v4);
        
        LcdImage.Builder b0 = new LcdImage.Builder(32, 3);
        LcdImage.Builder b1 = new LcdImage.Builder(32, 3);
        LcdImage.Builder b2 = new LcdImage.Builder(32, 3);
        LcdImage.Builder b3 = new LcdImage.Builder(32, 3);
        LcdImage.Builder b4 = new LcdImage.Builder(32, 3);
        LcdImage.Builder b5 = new LcdImage.Builder(32, 3);
        LcdImage.Builder b6 = new LcdImage.Builder(32, 3);
        LcdImage.Builder b7 = new LcdImage.Builder(32, 3);
        
        b0.setLine(l0, 0);
        b0.setLine(l0, 1);
        b0.setLine(l0, 2);
       
        b1.setLine(l1, 0);
        b1.setLine(l1, 1);
        b1.setLine(l1, 2);
        
        b2.setLine(l2, 0);
        b2.setLine(l2, 1);
        b2.setLine(l2, 2);
        
        b3.setLine(l3, 0);
        b3.setLine(l3, 1);
        b3.setLine(l3, 2);
        
        b4.setLine(l4, 0);
        b4.setLine(l4, 1);
        b4.setLine(l4, 2);
        
        b5.setLine(l5, 0);
        b5.setLine(l5, 1);
        b5.setLine(l5, 2);
        
        b6.setLine(l6, 0);
        b6.setLine(l6, 1);
        b6.setLine(l6, 2);
      
        b7.setLine(l7, 0);
        b7.setLine(l7, 1);
        b7.setLine(l7, 2);
        
        LcdImage i0a = b0.build();
        LcdImage i1a = b1.build();
        LcdImage i2a = b2.build();
        LcdImage i3a = b3.build();
        LcdImage i4a = b4.build();
        LcdImage i5a = b5.build();
        LcdImage i6a = b6.build();
        LcdImage i7a = b7.build();
        
        List<LcdImageLine> list0 = new ArrayList<>();
        for (int i = 0 ; i < 3 ; ++i) {
            list0.add(l0);
        }
        List<LcdImageLine> list1 = new ArrayList<>();
        for (int i = 0 ; i < 3 ; ++i) {
            list1.add(l1);
        }
        List<LcdImageLine> list2 = new ArrayList<>();
        for (int i = 0 ; i < 3 ; ++i) {
            list2.add(l2);
        }
        List<LcdImageLine> list3 = new ArrayList<>();
        for (int i = 0 ; i < 3 ; ++i) {
            list3.add(l3);
        }
        List<LcdImageLine> list4 = new ArrayList<>();
        for (int i = 0 ; i < 3 ; ++i) {
            list4.add(l4);
        }
        List<LcdImageLine> list5 = new ArrayList<>();
        for (int i = 0 ; i < 3 ; ++i) {
            list5.add(l5);
        }
        List<LcdImageLine> list6 = new ArrayList<>();
        for (int i = 0 ; i < 3 ; ++i) {
            list6.add(l6);
        }
        List<LcdImageLine> list7 = new ArrayList<>();
        for (int i = 0 ; i < 3 ; ++i) {
            list7.add(l7);
        }
        
        LcdImage i0 = new LcdImage(3, 32, list0);
        LcdImage i1 = new LcdImage(3, 32, list1);
        LcdImage i2 = new LcdImage(3, 32, list2);
        LcdImage i3 = new LcdImage(3, 32, list3);
        LcdImage i4 = new LcdImage(3, 32, list4);
        LcdImage i5 = new LcdImage(3, 32, list5);
        LcdImage i6 = new LcdImage(3, 32, list6);
        LcdImage i7 = new LcdImage(3, 32, list7);
        
        assertTrue(i0a.equals(i0));
        assertTrue(i1a.equals(i1));
        assertTrue(i2a.equals(i2));
        assertTrue(i3a.equals(i3));
        assertTrue(i4a.equals(i4));
        assertTrue(i5a.equals(i5));
        assertTrue(i6a.equals(i6));
        assertTrue(i7a.equals(i7));
    }
    
    // Cas d'erreur
    @org.junit.jupiter.api.Test
    public void builderError() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        
        LcdImage.Builder b0 = new LcdImage.Builder(32, 3);
        
        b0.setLine(l0, 0);
        b0.setLine(l0, 1);
        b0.setLine(l0, 2);
        
        LcdImage i0a = b0.build();
        
        assertThrows(IllegalStateException.class, () -> {b0.setLine(l0, 1);});
        assertThrows(IllegalStateException.class, () -> {b0.build();});
    }
    
    
    // TESTS GET
    // Cas normal
    @org.junit.jupiter.api.Test
    public void getNormal() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1 = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2 = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3 = new LcdImageLine(v0, v1, v0);
        
        List<LcdImageLine> list0 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list0.add(l0);
        }
        List<LcdImageLine> list1 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list1.add(l1);
        }
        List<LcdImageLine> list2 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list2.add(l2);
        }
        List<LcdImageLine> list3 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list3.add(l3);
        }
        
        LcdImage i0 = new LcdImage(54, 32, list0);
        LcdImage i1 = new LcdImage(54, 32, list1);
        LcdImage i2 = new LcdImage(54, 32, list2);
        LcdImage i3 = new LcdImage(54, 32, list3);
        
        for(int i = 0 ; i < 54 ; ++i) {
            for(int j = 0 ; j < 32 ; ++j) {
                assertEquals(0, i0.get(j, i));
            }
        }
        
        for(int i = 0 ; i < 54 ; ++i) {
            for(int j = 0 ; j < 32 ; ++j) {
                assertEquals(3, i1.get(j, i));
            }
        }
        
        for(int i = 0 ; i < 54 ; ++i) {
            for(int j = 0 ; j < 32 ; ++j) {
                assertEquals(2, i2.get(j, i));
            }
        }
        
        for(int i = 0 ; i < 54 ; ++i) {
            for(int j = 0 ; j < 32 ; ++j) {
                assertEquals(1, i3.get(j, i));
            }
        }
    }
    
    // Cas d'erreur
    @org.junit.jupiter.api.Test
    public void getError() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1 = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2 = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3 = new LcdImageLine(v0, v1, v0);
        LcdImageLine l4 = new LcdImageLine(v3, v3, v3);
        LcdImageLine l5 = new LcdImageLine(v4, v4, v4);
        LcdImageLine l6 = new LcdImageLine(v3, v4, v3);
        LcdImageLine l7 = new LcdImageLine(v4, v3, v4);
        
        List<LcdImageLine> list0 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list0.add(l0);
        }
        List<LcdImageLine> list1 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list1.add(l1);
        }
        List<LcdImageLine> list2 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list2.add(l2);
        }
        List<LcdImageLine> list3 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list3.add(l3);
        }
        List<LcdImageLine> list4 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list4.add(l4);
        }
        List<LcdImageLine> list5 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list5.add(l5);
        }
        List<LcdImageLine> list6 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list6.add(l6);
        }
        List<LcdImageLine> list7 = new ArrayList<>();
        for (int i = 0 ; i < 54 ; ++i) {
            list7.add(l7);
        }
        
        LcdImage i0 = new LcdImage(54, 32, list0);
        LcdImage i1 = new LcdImage(54, 32, list1);
        LcdImage i2 = new LcdImage(54, 32, list2);
        LcdImage i3 = new LcdImage(54, 32, list3);
        LcdImage i4 = new LcdImage(54, 32, list4);
        LcdImage i5 = new LcdImage(54, 32, list5);
        LcdImage i6 = new LcdImage(54, 32, list6);
        LcdImage i7 = new LcdImage(54, 32, list7);
        
        assertThrows(IndexOutOfBoundsException.class, () -> {i0.get(32, 54);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i1.get(32, 54);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i2.get(32, 54);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i3.get(32, 54);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i4.get(32, 54);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i5.get(32, 54);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i6.get(32, 54);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i7.get(32, 54);});
        
        assertThrows(IndexOutOfBoundsException.class, () -> {i0.get(-1, -1);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i1.get(-1, -1);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i2.get(-1, -1);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i3.get(-1, -1);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i4.get(-1, -1);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i5.get(-1, -1);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i6.get(-1, -1);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i7.get(-1, -1);});
        
        assertThrows(IndexOutOfBoundsException.class, () -> {i0.get(-3, 3);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i1.get(5, -2);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i2.get(88, 67);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i3.get(88, 67);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i4.get(1, 67);});
        assertThrows(IndexOutOfBoundsException.class, () -> {i5.get(67, 1);});
    }
}
