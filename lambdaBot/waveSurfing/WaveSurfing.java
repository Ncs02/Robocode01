
package jaara.waveSurfing;

import jaara.engine.BotState;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.*;
import robocode.*;
import robocode.util.Utils;

/**
 *
 * @author Jaromír Janisch <jaara.j@email.cz>
 */
public class WaveSurfing {
    private static AdvancedRobot robot;

    public WaveSurfing(AdvancedRobot r) {
        robot = r;
    }

    Collection<Wave> waves = new LinkedList<Wave>();
    static Probability probability = new Probability(21, 5);

    static{
        //HEAD ON GUN FIX
        probability.add(0.5);
    }

    public Collection<Wave> getWaves() {
        return waves;
    }

    /**
     * Gives a wave nearest to us
     * @param pos
     * @return nearest wave
     */
    private Wave getNearestWave(Point2D.Double pos){
        long time = robot.getTime();
        //Point2D.Double pos = new Point2D.Double(robot.getX(), robot.getY());

        Wave nearest = null;
        double nearestDist = Double.MAX_VALUE;
        
        for(Wave w: waves){
            double dist = Math.abs(w.getOrigin().distance(pos) - w.getDistance(time));

            if(nearest == null){
                nearest = w;
                nearestDist = dist;
                continue;
            }

            if(nearestDist > dist){
                nearest = w;
                nearestDist = dist;
            }
        }

        return nearest;
    }

    /**
     * Fires when a bullet hits us.
     */
    public void onHit(){
        Point2D.Double pos = new Point2D.Double(robot.getX(), robot.getY());
        Wave w = getNearestWave(pos);
        
        if(w == null)
            return;

        long time = robot.getTime();

        double dist = w.getOrigin().distance(pos) - w.getDistance(time);
        if(Math.abs(dist) > 30)
            return;

        double dx = pos.x-w.getOrigin().x;
        double dy = pos.y-w.getOrigin().y;

        double realAngle = Math.atan2(dx, dy);
        double diffAngle = realAngle - w.getBearing();
        
        probability.add(normalizeAngle(diffAngle));

        waves.remove(w);
    }

    /**
     * Fires when a bullet hits another bullet, from this information we can construct original angle.
     * @param pos
     */
    public void onBulletHitBullet(Point2D.Double pos){
        Wave w = getNearestWave(pos);

        if(w == null){
            System.out.println("Can't find wave!");
        }

        double dist = w.getOrigin().distance(pos) - w.getDistance(robot.getTime());
        if(Math.abs(dist) > 15)
            return;

        double dx = pos.x-w.getOrigin().x;
        double dy = pos.y-w.getOrigin().y;

        double realAngle = Math.atan2(dx, dy);
        double diffAngle = realAngle - w.getBearing();

        probability.add(normalizeAngle(diffAngle));

        waves.remove(w);
    }

    /**
     * Normalizes angle to interval <0,1> for use with probability class. Uses maximum possible escape angle.
     * @param angle
     * @return
     */
    public double normalizeAngle(double angle){
        double maxAngle = getMaxEscapeAngle(3.0);
        
        angle = Utils.normalRelativeAngle(angle);

        angle /= (2.0*maxAngle);
        angle += 0.5;

        return angle;
    }

    /**
     * On enemy fire event
     * @param enemyLoc current enemy location
     * @param firePower power of new bullet
     */
    public void onEnemyFire(Point2D.Double enemyPosition, double firePower){
        Point2D.Double pos = stateLast.getPosition();
        double dx = pos.x-enemyPosition.x;
        double dy = pos.y-enemyPosition.y;

        double bearing = Math.atan2(dx, dy);

        //  LINEAR GUN FIX
        
        Point2D.Double nextPosition = pos;
        double width = robot.getBattleFieldWidth();
        double height = robot.getBattleFieldHeight();
        double edge = 18.0;
        while(true){
            double impactTime = nextPosition.distance(enemyPosition) / Rules.getBulletSpeed(firePower);

            Point2D.Double oldPosition = nextPosition;
            double k = stateLast.getVelocity() * impactTime;
            nextPosition = new Point2D.Double(
                pos.x + Math.sin(stateLast.getHeading()) * k,
                pos.y + Math.cos(stateLast.getHeading()) * k
            );

            if(nextPosition.x > width - edge){
                nextPosition.x = width - edge;
            }
            else if(nextPosition.x < edge){
//                nextPosition.x = edge;
                break;
            }
            if(nextPosition.y > height - edge){
//                nextPosition.y = height - edge;
                break;
            }
            else if(nextPosition.y < edge){
//                nextPosition.y = edge;
                break;
            }

            double dist = oldPosition.distance(nextPosition);
            if(dist < 0.1)
                break;

            impactTime = nextPosition.distance(enemyPosition) / Rules.getBulletSpeed(firePower);
        }
        double nextdx = nextPosition.x-enemyPosition.x;
        double nextdy = nextPosition.y-enemyPosition.y;

        double nextBearing = Math.atan2(nextdx, nextdy);

        Wave w = new Wave(enemyPosition, firePower, bearing, robot.getTime()-1, nextBearing);
        waves.add(w);

        //Renderable.drawPoint(nextPosition, Color.WHITE);
    }

    private BotState stateLast;
    private BotState stateNow;

    /**
     * Main entry point, initialization
     */
    public void run(){
        updateWaves();

        stateLast = stateNow;
        stateNow = new BotState(new Point2D.Double(robot.getX(), robot.getY()),
                robot.getEnergy(), robot.getVelocity(), robot.getHeadingRadians());
//        System.out.println(probability);
    }

    public double getProbability(double angleMin, double angleMax){
        return probability.getProbability(normalizeAngle(angleMin), normalizeAngle(angleMax));
    }

    /**
     * Removes waves past the target.
     */
    private void updateWaves(){
        long time = robot.getTime();
        Point2D.Double pos = new Point2D.Double(robot.getX(), robot.getY());

        Iterator<Wave> i = waves.iterator();
        while(i.hasNext()){
            Wave w = i.next();
            double dist = w.getOrigin().distance(pos) - w.getDistance(time);

            if(dist < -50)
                i.remove();

            Renderable.drawCircle(w.getOrigin(), w.getDistance(time), Color.YELLOW);
            Renderable.drawLine(w.getOrigin(), project(w.getOrigin(), w.getBearing(), w.getDistance(time)), Color.BLACK);
            Renderable.drawLine(w.getOrigin(), project(w.getOrigin(), w.getLinarGunAngle(), w.getDistance(time)), Color.BLUE);
        }
    }

    private Point2D.Double project(Point2D.Double origin, double angle, double dist){
        return new Point2D.Double(
                origin.x + Math.sin(angle)*dist,
                origin.y + Math.cos(angle)*dist
                );
    }

    /**
     * Simple computation of max escape angle.
     * @param bulletPower
     * @return
     */
    private double getMaxEscapeAngle(double bulletPower){
        double bulletSpeed = Rules.getBulletSpeed(bulletPower);
        double angle = Math.asin(8.0 / bulletSpeed);

        return angle;
    }

}