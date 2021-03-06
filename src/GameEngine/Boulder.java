package GameEngine;

import GameEngine.CollisionHandler.*;
import GameEngine.utils.Direction;
import GameEngine.utils.Point;

public class Boulder extends StandardObject implements Movable{
    public static final double SPEED = Player.SPEED / 2;
    private Direction facing;
    private double speed;

    /**
     * constructor for Boulder object
     * @param location
     */
    public Boulder(Point location) {
        super(location);
        this.speed = 0;
    }

    /**
     * Always true because Boulder always blocks
     * @return true
     */
    @Override
    public boolean isBlocking() {
        return true;
    }

    /**
     * Get facing
     * Returns the direction the object is facing
     * @return
     */
    @Override
    public Direction getFacing() {
        return this.facing;
    }

    /**
     * set boulder's direction same as player's
     * 
     * @param facing new facing to be set
     */
    public void setFacing(Direction facing) {
        this.facing = facing;
    }

    /**
     * set speed
     * 
     * @param speed new speed
     */
    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    @Override
    public double getSpeed() {
        return this.speed;
    }

    /**
     * set boulder's speed to 0 and s
     * @param point
     *          point
     */
    @Override
    public boolean setLocation(Point point) {
        this.setSpeed(0.0);
        return super.setLocation(point);
    }

    /**
     * If a boulder moved, update monsters' path
     * since it might block/unblock current path
     *
     * @param engine game engine
     */
    @Override
    public void onUpdatingLocation(GameEngine engine) {
        engine.updateMonstersPath();
    }

    /**
     * Register collision handler for boulder
     */
    @Override
    public void registerCollisionHandler(GameEngine gameEngine) {
        // boulder and monster
        gameEngine.registerCollisionHandler(new CollisionEntities(this.getClass(), Monster.class),
                new BoulderMonsterCollisionHandler());

        // handler for boulder with pit
        gameEngine.registerCollisionHandler(new CollisionEntities(this.getClass(), Pit.class),
                new BoulderPitCollisionHandler());

    }
}
