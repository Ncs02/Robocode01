package jaara.engine;

import jaara.waveSurfing.Wave;
import java.awt.geom.Point2D;
import java.util.*;
import robocode.Rules;
import robocode.util.Utils;


// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.174DD7DA-BB76-4016-5BBF-91D35BD7C14D]
// </editor-fold> 
public class World {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.049A01BF-B6E1-B83A-94A8-D0A4C2498323]
    // </editor-fold> 
    private BotState me;
    private BotState enemy;

    private double steering;
    private double accelerate;
    private int time;

    Collection<Wave> waves;

    public void accelerateMe(double v){
        accelerate += v;
        
        double desired = me.getVelocity()+accelerate;
        double desiredDiff = capAbs(desired, Rules.MAX_VELOCITY);
        accelerate = accelerate - (desired - desiredDiff);
    }
    public void turnMe(double v){
        steering = v;
    }

    public double getDesiredVelocity(){
        return me.getVelocity()+accelerate;
    }

    public boolean isSteering(){
        return steering != 0.0;
    }

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.F244FEE3-69FF-C361-04AB-8506A6528DA0]
    // </editor-fold> 
    public void run (int ticks) {
        for(int i=0; i<ticks; i++, time++){

            double steer = capAbs(steering, Rules.getTurnRateRadians(me.getVelocity()));
            steering -= steer;
            me.setHeading( me.getHeading() + steer);

            double accel = 0.0;
            if((me.getVelocity() >= 0 && accelerate > 0) ||
                    me.getVelocity() <= 0 && accelerate < 0){
                accel = capAbs(accelerate, Rules.ACCELERATION);
                accelerate -= accel;
            }
            else{
                accel = capAbs(accelerate, Rules.DECELERATION);
                accelerate -= accel;
            }

            double velocity = me.getVelocity()+accel;
            velocity = capAbs(velocity, Rules.MAX_VELOCITY);

            me.setVelocity(velocity);
            
            //System.out.println("HEADING: "+me.getHeading());

            Point2D.Double pos = me.getPosition();
            pos.x += me.getVelocity() * Math.sin(me.getHeading());
            pos.y += me.getVelocity() * Math.cos(me.getHeading());

            final double edge = 20;
            double width = Engine.getRobot().getBattleFieldWidth();
            double height = Engine.getRobot().getBattleFieldHeight();

            if(pos.x > width - edge){
                pos.x = width - edge;
                me.setLife(me.getLife() - Rules.getWallHitDamage(me.getVelocity()));
                me.setVelocity(0.0);
            }
            else if(pos.x < edge){
                pos.x = edge;
                me.setLife(me.getLife() - Rules.getWallHitDamage(me.getVelocity()));
                me.setVelocity(0.0);
            }

            if(pos.y > height - edge){
                pos.y = height - edge;
                me.setLife(me.getLife() - Rules.getWallHitDamage(me.getVelocity()));
                me.setVelocity(0.0);
            }
            else if(pos.y < edge){
                pos.y = edge;
                me.setLife(me.getLife() - Rules.getWallHitDamage(me.getVelocity()));
                me.setVelocity(0.0);
            }

            Wave w = getNearestWave();
            if(w == null)
                continue;

            double distToMe = w.getOrigin().distance(me.getPosition());
            double dist = distToMe - w.getDistance(time);
            if(Math.abs(dist) > 20)
                continue;

            double dx = me.getPosition().x-w.getOrigin().x;
            double dy = me.getPosition().y-w.getOrigin().y;

            double realAngle = Math.atan2(dx, dy);
            double diffAngle = realAngle - w.getBearing();

            final double botWidth = 50;
            double delta = Math.atan2(botWidth/2.0, distToMe);

            double prob = 0.0;
            try{
                prob = Engine.getRobot().getWaveSurfing().getProbability(diffAngle-delta, diffAngle+delta);
            }
            catch(IllegalArgumentException e){
                System.out.println("DEBUG: Wrong angle...");
            }
            
            if(realAngle >= w.getLinarGunAngle()-delta && realAngle <= w.getLinarGunAngle()+delta)
                prob += 0.1;    //LINEAR GUN FIX

            double damage = prob * Rules.getBulletDamage(w.getPower());

            me.setLife(me.getLife() - damage);

            waves.remove(w);
        }
    }

    public Wave getNearestWave(){
        Point2D.Double pos = me.getPosition();

        Wave nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for(Wave w: waves){
            double dist = w.getOrigin().distance(pos) - w.getDistance(time);

            if(dist < -18)
                continue;

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

    private double capAbs(double val, double cap){
        if(val < 0){
            if(val < -cap)
                return -cap;
            else
                return val;
        }
        else{
            if(val > cap)
                return cap;
            else
                return val;
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.90CE5B0C-1631-2B01-BFBD-A20DD8B4BB07]
    // </editor-fold> 
    public double evaluate () {
        double value = 0.0;

        Point2D.Double center = new Point2D.Double(Engine.getRobot().getBattleFieldWidth()/2, Engine.getRobot().getBattleFieldHeight()/2);
        value += center.distance(me.getPosition()) / 600.0;

        final double idealDistance = 500;
        value += Math.abs(me.getPosition().distance(enemy.getPosition())-idealDistance) / 500.0;

        //bearing to enemy
        double absAngle = Math.atan2( me.getPosition().x-enemy.getPosition().x, me.getPosition().y-enemy.getPosition().y);
        double angle = Math.abs(Utils.normalRelativeAngle(me.getHeading() - absAngle));

        value += Math.abs(Math.PI/2.0-angle) / 100;

        value += -me.getLife();

        return value;

//        double x = me.getPosition().x;
//        double y = me.getPosition().y;
//        double val = Math.abs(x-500.0)+Math.abs(y-500.0);
//       // System.out.println(val);
//        return val;

        //return me.getPosition().x+me.getPosition().y;*/
    }

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.32E9B67A-77C2-4289-4048-96AB43810163]
    // </editor-fold> 
    public World (BotState me) {
        this.me = me;
    }

    public World(BotState me, double steering, double accelerate, int time, Collection<Wave> waves, BotState enemy) {
        this.me = me;
        this.steering = steering;
        this.accelerate = accelerate;
        this.time = time;

        this.waves = new LinkedList(waves);
        this.enemy = enemy;
    }


    @Override
    protected World clone(){
        World newWorld = new World(me.clone(), steering, accelerate, time, waves, enemy.clone());

        return newWorld;
    }

    public BotState getMe() {
        return me;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash += time;
        hash *= 10;
        hash += steering * 5;
        hash *= 10;
        hash += accelerate / 2;
        hash *= 1000;
        
        hash += me.hashCode();

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof World))
            return false;

        World w = (World)obj;

        if( Math.abs(steering-w.steering) < 0.1 && Math.abs(accelerate-w.accelerate) < 1 &&
                me.equals(w.me) && time == w.time)
            return true;

        return false;
    }
}

