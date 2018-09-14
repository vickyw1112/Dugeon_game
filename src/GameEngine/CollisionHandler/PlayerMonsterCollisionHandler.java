package GameEngine.CollisionHandler;


import GameEngine.*;
import GameEngine.utils.PlayerEffect;

public class PlayerMonsterCollisionHandler implements CollisionHandler {

    @Override
    public CollisionResult handle(GameEngine engine, GameObject obj1, GameObject obj2){
        Player player = (Player) (obj1 instanceof Player ? obj1 : obj2);
        CollisionResult res = new CollisionResult();

        if (player.getPlayerEffects().contains(PlayerEffect.INVINCIBLE)) {
            res.addFlag(CollisionResult.HANDLED);
            return res;
        } else if (player.getInventory().getCount(Sword.class) > 0) {
            int n = player.getInventory().getCount(Sword.class);
            player.getInventory().setCount(Sword.class, n - 1);
            res.addFlag(CollisionResult.HANDLED);
            return res;
        } else {
            res.addFlag(CollisionResult.LOSE);
            return res;
        }
    }

}