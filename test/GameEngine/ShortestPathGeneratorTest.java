package GameEngine;

import GameEngine.utils.Point;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.*;

public class ShortestPathGeneratorTest {

    private Player player;
    private GameEngine engine;
    private Map map;
    private Monster monster;
    private Wall wall0;
    private Wall wall2;
    private Wall wall4;
    private Wall wall5;
    private Wall wall6;
    private Wall wall7;
    private MapBuilder mb;

    @Before
    public void setUp() {
        mb = new MapBuilder();
        player = new Player(new Point(0, 0));
        wall0 = new Wall(new Point(0,1));
        wall2 = new Wall(new Point(2,1));
        wall4 = new Wall(new Point(4,1));
        wall5 = new Wall(new Point(5, 1));
        wall6 = new Wall(new Point(6, 1));
        wall7 = new Wall(new Point(7,1 ));
        monster = new Hunter(new Point(0,2));


        mb.addObject(player);
        mb.addObject(wall0);
//        mb.addObject(wall1);
        mb.addObject(wall2);
//        mb.addObject(wall3);
        mb.addObject(wall4);
        mb.addObject(wall5);
        mb.addObject(wall6);
        mb.addObject(wall7);

        map = new Map(mb);
        engine = new GameEngine(map);
        player.initialize();
    }

    /**
     * checkBasicBFS method
     * checks the shortest path generation
     */
    @Test
    public void checkBasicBFS() {
        long start = System.nanoTime();
        engine.updateMonstersPath();
        LinkedList<Point> path = map.getShortestPath(monster.getLocation(), player.getLocation());
        LinkedList<Point> check = new LinkedList<>();

        check.add(new Point(1,2));
        check.add(new Point(1,1));
        check.add(new Point(1,0));
        check.add(new Point(0,0));

        assertEquals(check, path);
    }

    /**
     * checkBoulderBFS method
     * checks path generation with boulder as obstacle.
     */
    @Test
    public void checkBoulderBFS() {
        Boulder boulder = new Boulder(new Point(1,1 ));
        mb.addObject(boulder);
        map = new Map(mb);
        engine.updateMonstersPath();
        LinkedList<Point> path = map.getShortestPath(monster.getLocation(), player.getLocation());
        LinkedList<Point> check = new LinkedList<>();

        check.add(new Point(1,2));
        check.add(new Point(2,2));
        check.add(new Point(3,2));
        check.add(new Point(3,1));
        check.add(new Point(3,0));
        check.add(new Point(2,0));
        check.add(new Point(1,0));
        check.add(new Point(0,0));

        assertEquals(check, path);
    }

    /**
     * checkPitBFS method
     * checks path generation with Pit as an obstacle
     */
    @Test
    public void checkPitBFS() {
        Pit pit = new Pit(new Point(1,1));
        mb.addObject(pit);
        map = new Map(mb);
        long start = System.nanoTime();
        engine.updateMonstersPath();
        LinkedList<Point> path = map.getShortestPath(monster.getLocation(), player.getLocation());
        LinkedList<Point> check = new LinkedList<>();

        check.add(new Point(1,2));
        check.add(new Point(2,2));
        check.add(new Point(3,2));
        check.add(new Point(3,1));
        check.add(new Point(3,0));
        check.add(new Point(2,0));
        check.add(new Point(1,0));
        check.add(new Point(0,0));

        assertEquals(check, path);
    }


    /**
     * checkDoorBFS method
     * checks the path generation with a door as obstacle
     */
    @Test
    public void checkDoorBFS() {
        Door door = new Door(new Point(1,1));
        mb.addObject(door);
        map = new Map(mb);
        engine.updateMonstersPath();
        LinkedList<Point> path = map.getShortestPath(monster.getLocation(), player.getLocation());
        LinkedList<Point> check = new LinkedList<>();

        check.add(new Point(1,2));
        check.add(new Point(2,2));
        check.add(new Point(3,2));
        check.add(new Point(3,1));
        check.add(new Point(3,0));
        check.add(new Point(2,0));
        check.add(new Point(1,0));
        check.add(new Point(0,0));

        assertEquals(check, path);
    }
}

