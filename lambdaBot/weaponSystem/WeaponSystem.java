
package jaara.weaponSystem;

import jaara.engine.BotState;
import jaara.waveSurfing.*;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.*;
import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.util.Utils;

/**
 *
 * @author Jaromír Janisch <jaara.j@email.cz>
 */
public class WeaponSystem {
    private static Probability probability = new Probability(41, 10);

    static{
        probability.add(0.5);
    }

    private double firePower = 2.0;

    public double getFirePower() {
        return firePower;
    }

//    public void setFirePower(double firePower) {
//        this.firePower = firePower;
//    }

    

    private AdvancedRobot robot;

    BotState me;
    BotState enemy;

//    double[] deviance = new double[50];
//    int devWriter = 0;
//    private double computeAverage(){
//        double ex = 0;
//        for(double d : deviance){
//            ex += 1/d;
//        }
//        return deviance.length / ex;
//    }
//    private double computeSigma(){
//        double e2x = 0;
//        double ex2 = 0;
//        for(double d : deviance){
//            ex2 += d*d;
//            e2x += d;
//        }
//        ex2 /= deviance.length;
//        e2x /= deviance.length;
//
//        e2x = e2x * e2x;
//
//        return Math.sqrt(ex2 - e2x);
//
//    }

    private class WaveHolder{
        public Wave wave;
        public BotState botState;

        public WaveHolder(Wave wave, BotState botState) {
            this.wave = wave;
            this.botState = botState;
        }        
    }
    Collection<WaveHolder> waves = new LinkedList<WaveHolder>();

    public WeaponSystem(AdvancedRobot robot) {
        this.robot = robot;
    }

    /**
     * Statistical function
     * @return estimated hitting probability
     */
    public double getHitProbability() {
        return hitProbability;
    }

    /**
     * Main entry point
     */
    public void run(){

        if(enemy.getLife() < 4){
            firePower = enemy.getLife()/4.0;
        }
        else if(enemy.getLife() < 10){
            firePower = (enemy.getLife()+2)/6.0;
        }
        else
            firePower = 2.0;

        updateWaves();

        if(enemy.getPosition().x == -1)
            return;

        double angle = Utils.normalRelativeAngle(getFiringAngle() - robot.getGunHeadingRadians());
        robot.setTurnGunRightRadians( angle );

        double bearing = Math.atan2(enemy.getPosition().x - me.getPosition().x, enemy.getPosition().y - me.getPosition().y);

//        System.out.println("Av: "+computeAverage()+" Dev:"+computeSigma());
        Wave w = new Wave(me.getPosition(), firePower, bearing, robot.getTime(), getFiringAngle());

        waves.add(new WaveHolder(w, enemy));

        double[] d = getMaxEscapeAngle(firePower, me.getPosition(), enemy);
        double dist = me.getPosition().distance(enemy.getPosition());
        if(!isClockwise(me.getPosition(), enemy)){
            d[1] = -d[1];
            d[0] = -d[0];
        }
//        System.out.println( isClockwise(me.getPosition(), enemy) ? "CLOCK" : "COUNTER-CLOCK" );
        Point2D.Double p1 = new Point2D.Double(
                Math.sin(bearing + d[0]) * dist + me.getPosition().x,
                Math.cos(bearing + d[0]) * dist + me.getPosition().y
                );
        Point2D.Double p2 = new Point2D.Double(
                Math.sin(bearing + d[1]) * dist + me.getPosition().x,
                Math.cos(bearing + d[1]) * dist + me.getPosition().y
                );
        Renderable.drawLine(me.getPosition(), p1, Color.red);
        Renderable.drawLine(me.getPosition(), p2, Color.red);

//        System.out.println(probability);
        System.out.println("HIT PROBABILITY: "+hitProbability);
    }

    /**
     * Removes obsolete waves
     */
    private void updateWaves(){
        long time = robot.getTime();       

        Iterator<WaveHolder> i = waves.iterator();

        while(i.hasNext()){
            WaveHolder wh = i.next();
            Wave w = wh.wave;

            double dist = w.getOrigin().distance(enemy.getPosition());

            if(w.getDistance(time) > dist - 20){ //wave is breaking...
                i.remove();

                double newBearing = Math.atan2(enemy.getPosition().x - w.getOrigin().x, enemy.getPosition().y - w.getOrigin().y);
                double oldBearing = w.getBearing();

                double diff = newBearing - oldBearing;
                probability.add(normalizeAngle(diff, w.getPower(), w.getOrigin(), wh.botState));

//                deviance[devWriter++] = Utils.normalAbsoluteAngle(newBearing - w.getLinarGunAngle());
//                devWriter %= deviance.length;

                final double botWidth = 40;
                double delta = Math.atan2(botWidth/2.0, dist);
                final double step = 0.01;
                if( newBearing > oldBearing-delta && newBearing < oldBearing+delta){    //we did it right!
                    hitProbability += step;
                    if(hitProbability > 1.0)
                        hitProbability = 1.0;
                }
                else{
                    hitProbability -= step;
                    if(hitProbability < 0.0)
                        hitProbability = 0.0;
                }
            }
        }
    }

    private double hitProbability = 0.5;

    /**
     * can we fire in this time?
     * @param time
     * @return
     */
    public boolean canFire(long time){
        int td = (int) (time - robot.getTime());

        if ( robot.getGunHeat() - td * robot.getGunCoolingRate() <= 0)
            return true;

        return false;
    }

    /**
     * fires a bullet
     */
    public void fire(){
        if(!canFire(robot.getTime()))
            return;

        double angle = Utils.normalRelativeAngle(getFiringAngle() - robot.getGunHeadingRadians());
        robot.setTurnGunRightRadians( angle );

        robot.setFire(firePower);
    }

    private double getFiringAngle(){
        double bearing = Math.atan2(enemy.getPosition().x - me.getPosition().x, enemy.getPosition().y - me.getPosition().y);

        double s = probability.getMostProbableSegment();
        double angle = denormalizeSegment(firePower, s) + bearing;

//        if(computeSigma() < 0.05)
//            angle += computeAverage();
        
        return angle;
    }

    public void update(BotState me, BotState enemy){
        this.me = me;
        this.enemy = enemy;
    }

    public double normalizeAngle(double angle, double firePower, Point2D.Double mePosition, BotState enemy){
        double[] angles = getMaxEscapeAngle(firePower, mePosition, enemy);

        double angleMin = angles[0];
        double angleMax = angles[1];
        double angleSize = angleMax - angleMin;

        if(!isClockwise(mePosition, enemy))
            angle *= -1;

        angle = Utils.normalRelativeAngle(angle);

        angle -= angleMin;
        angle /= angleSize;

        if(angle > 1.0){
            System.out.println("WS: bad angle "+ angle);
            angle = 1.0;
        }
        else if(angle < 0.0){
            System.out.println("WS: bad angle "+ angle);
            angle = 0.0;
        }

        return angle;
    }

    public double denormalizeSegment(double firepower, double s){
        double[] angles = getMaxEscapeAngle(firePower, me.getPosition(), enemy);

        double angleMin = angles[0];
        double angleMax = angles[1];
        double angleSize = angleMax - angleMin;

        s *= angleSize;
        s += angleMin;

        if(!isClockwise(me.getPosition(), enemy))
            s *= -1;

        s = Utils.normalRelativeAngle(s);

        return s;
    }

    /**
     * Precise computation of maximum escape angle
     * @param bulletPower
     * @param mePosition
     * @param enemy
     * @return min angle (negative) and max angle (positiove)
     */
    private double[] getMaxEscapeAngle(double bulletPower, Point2D.Double mePosition, BotState enemy){
        double vb = Rules.getBulletSpeed(bulletPower);
        double vm = Rules.MAX_VELOCITY;
        double v  = enemy.getVelocity();

        if(v < 0)
            v = -v;

        double dv = vm - v;
        double d  = v/2;
        
        double dist = mePosition.distance(enemy.getPosition());
        double t = dist * Math.sqrt( 1 + (vm*vm)/(vb*vb) ) / vb;   //first estimate
//        double t = dist/vb;

        if(t <= 12){
            double angle = Math.asin(8.0 / vb);
            return new double[] {-angle, angle};
        }

        double t1 = t;
        double t2 = t;
        double maxDist = dv * dv / 2 + vm * (t1 - dv);
        double minDist = d * d + vm * vm / 2 - vm * (t2 - d);
        
//        for(int i=0; i<4; i++){
//            t1 = Math.sqrt(dist*dist + maxDist*maxDist) / vb;
//            t2 = Math.sqrt(dist*dist + minDist*minDist) / vb;
//
//            maxDist = dv * dv / 2 + vm * (t1 - dv);
//            minDist = d * d + vm * vm / 2 - vm * (t2 - d);
//        }
        
/*
        if(enemy.getVelocity() < 0){
            double h = maxDist;
            maxDist = -minDist;
            minDist = -h;
        }*/

        double maxAngle = Math.sin(maxDist/dist);
        double minAngle = Math.sin(minDist/dist);

        double[] result = {minAngle, maxAngle};
        return result;
    }


    /**
     * Returns the direction constructed from our position, enemy position and enemy velocity
     * @param p1
     * @param enemy
     * @return true - clockwise, false - counter clockwise
     */
    private boolean isClockwise(Point2D.Double p1, BotState enemy){
        Point2D.Double p2 = enemy.getPosition();
        Point2D.Double p3 = new Point2D.Double(
                Math.sin(enemy.getHeading()) * enemy.getVelocity() + p2.x,
                Math.cos(enemy.getHeading()) * enemy.getVelocity() + p2.y
                );

        Point2D.Double e1 = new Point2D.Double(p1.x-p2.x,p1.y-p2.y);
        Point2D.Double e2 = new Point2D.Double(p3.x-p2.x,p3.y-p2.y);

        if( e1.x * e2.y - e1.y * e2.x >= 0 )
            return true;

        return false;
    }
}
