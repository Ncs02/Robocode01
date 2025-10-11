
package jaara.engine;

import robocode.AdvancedRobot;

/**
 *
 * @author Jaromír Janisch <jaara.j@email.cz>
 */
public class Stop extends Action{

    @Override
    public World run(World w) {
        World newWorld = w.clone();
        newWorld.accelerateMe(-newWorld.getDesiredVelocity());
        newWorld.turnMe(0);

        return newWorld;
    }

    @Override
    public boolean isRunnable(World w) {
        return (w.getDesiredVelocity() != 0.0) ? true : false;
    }

    @Override
    public void runReally() {
        AdvancedRobot robot = Engine.getRobot();
        robot.setMaxVelocity(0.0);
        robot.setTurnRightRadians(0.0);
    }

    @Override
    public String toString() {
        return "STOP";
    }



}
