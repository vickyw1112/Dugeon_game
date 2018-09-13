package GameEngine;

import static org.junit.Assert.*;

import GameEngine.utils.Point;
import org.junit.BeforeClass;

import org.junit.Test;

public class InventoryTest {

    static GameEngine engine;
    static Player player;
    static Inventory inv;

    @BeforeClass
    static public void beforeTest(){
        engine = new GameEngine(new Map());
        player = new Player(new Point(5,5));
        inv = new Inventory();
    }
    
    /**
     * Checks the contain function in Inventory.java
     * @throws Exception
     */
    @Test
    public void containsTest() {
        Key key = new Key(new Point(1,1));
        assertTrue(!inv.contains(key));
        
        inv.addObject(key);
        assertTrue(inv.contains(key));
    }
    
    /**
     * Checks getCount method in Inventory.java
     * @throws Exception
     */
    @Test
    public void getCountTest() {
        Arrow arrow = new Arrow(new Point(2,2));
        assertEquals(inv.getCount("Arrow"), 0);
        
        inv.addObject(arrow);
        assertEquals(inv.getCount("Arrow"), 1);
        
        //popObject takes in string?
        inv.popObject("Arrow");
        assertEquals(inv.getCount("Arrow"), 0);
    }
    
    /**
     * Check setCount method in Inventory.java
     * @throws Exception
     */
    @Test
    public void setCountTest() {
        Inventory inv1 = new Inventory();
        inv1.setCount("Arrow", 3);  
        assertEquals(inv1.getCount("Arrow"), 3);
        
    }
    
    
    /**
     * Checks the addObject method, kind of redudant beacuse of getCount check
     * @throws Exception
     */
    @Test
    public void addObjectTest() {
        Treasure t = new Treasure(new Point(1,1));
        inv.addObject(t);
        assertEquals(inv.getCount("Treasure"), 1);
    }
    
    
    /**
     * Checks the popObject method
     * @throws Exception
     */
    @Test
    public void popObjectTest() {
        Sword sword = new Sword(new Point(1,1));
        inv.addObject(sword);
        assertEquals(inv.popObject("Sword"), sword);
    }
}
