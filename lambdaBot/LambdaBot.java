
package jaara;

import jaara.engine.*;
import jaara.waveSurfing.*;
import jaara.weaponSystem.WeaponSystem;
import java.awt.Graphics2D;
import robocode.*;
import java.awt.Color;
import java.awt.geom.Point2D;
import robocode.util.Utils;


/**
 * The main class for the bot.
 *
 * @author Jaromír Janisch <jaara.j@email.cz>
 */
public class LambdaBot extends AdvancedRobot{

    private Engine engine = new Engine(this);
    private WaveSurfing waveSurfing = new WaveSurfing(this);
    private WeaponSystem weaponSystem = new WeaponSystem(this);

    public WaveSurfing getWaveSurfing() {
        return waveSurfing;
    }

    /**
     * The main entry point for the bot.
     */
    @Override
    public void run() {
        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForRobotTurn(true);
        setAllColors(Color.black);

        bHeight = super.getBattleFieldHeight();
        bWidth = super.getBattleFieldWidth();

        //weaponSystem.setFirePower(2.0);

        while(true){
            time = super.getTime();
            x = super.getX();
            y = super.getY();

            if(getRadarTurnRemainingRadians() == 0.0){
                setTurnRadarLeft(Double.POSITIVE_INFINITY);
            }

            //System.out.println("TURN: "+ getTime());
            waveSurfing.run();
            weaponSystem.run();
            engine.run();

            if(getEnergy() > weaponSystem.getFirePower() && enemySeen)
                    weaponSystem.fire();

            if(maxVelocity > 0){
                setAhead(10000.0);
            }
            else{
                setAhead(-10000.0);
            }
            enemySeen = false;
            execute();
        }
    }
    private boolean enemySeen = false;

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        waveSurfing.onHit();

        if(enemy != null){
            enemy.setLife( enemy.getLife() + Rules.getBulletHitBonus(event.getBullet().getPower()));
        }
    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent event) {
        Point2D.Double pos = new Point2D.Double(event.getBullet().getX(), event.getBullet().getY());
        waveSurfing.onBulletHitBullet(pos);
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        enemy.setLife(event.getEnergy());
    }

    private BotState enemy = new BotState(new Point2D.Double(-1,-1), 100, 0, 0);
    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        enemySeen = true;

        BotState oldEnemy = enemy;

        Point2D.Double enemyPosition = project(getX(), getY(), event.getBearingRadians()+getHeadingRadians(), event.getDistance());
        enemy = new BotState(enemyPosition, event.getEnergy(), event.getVelocity(), event.getHeadingRadians());


        double energyDiff = oldEnemy.getLife() - enemy.getLife();

        if(energyDiff > 0.0 && energyDiff <= 3.0)
            waveSurfing.onEnemyFire(enemyPosition, energyDiff);

        double radarHeading = getRadarHeadingRadians();
        double targetHeading = getHeadingRadians() + event.getBearingRadians();

        double radarTurn = Utils.normalRelativeAngle(targetHeading - radarHeading);

        double turn = (radarTurn < 0) ? -Math.PI/8.0 + radarTurn : Math.PI/8.0 + radarTurn;

        setTurnRadarRightRadians(turn);
    }

    /**
     * Function allows to project a point from origin and with given angle and distance
     * @param x origin x
     * @param y origin y
     * @param angle
     * @param dist distance
     * @return projected point
     */
    private Point2D.Double project(double x, double y, double angle, double dist){
        return project( new Point2D.Double(x,y), angle, dist);
    }
    private Point2D.Double project(Point2D.Double origin, double angle, double dist){
        return new Point2D.Double(
                origin.x + Math.sin(angle)*dist,
                origin.y + Math.cos(angle)*dist
                );
    }

    private double maxVelocity = 0;

    @Override
    public void setMaxVelocity(double newMaxVelocity) {
        if(newMaxVelocity > Rules.MAX_VELOCITY)
            newMaxVelocity = Rules.MAX_VELOCITY;
        else if(newMaxVelocity < -Rules.MAX_VELOCITY)
            newMaxVelocity = -Rules.MAX_VELOCITY;

        maxVelocity = newMaxVelocity;

        super.setMaxVelocity(Math.abs(newMaxVelocity));
    }

    public double getMaxVelocity(){
        return maxVelocity;
    }

    @Override
    public void onStatus(StatusEvent e) {
        RobotStatus s = e.getStatus();
        BotState me = new BotState(new Point2D.Double(s.getX(), s.getY()), s.getEnergy(), s.getVelocity(), s.getHeadingRadians());
        //System.out.println("VELOCITY: "+s.getVelocity());

        double acceleration = maxVelocity - s.getVelocity();
        double steering = getTurnRemainingRadians();

        World w = new World(me, steering, acceleration, (int)time, waveSurfing.getWaves(), enemy);
        engine.updateWorld(w);

        weaponSystem.update(me, enemy);
    }

    private long time;
    private double x;
    private double y;
    private double bWidth;
    private double bHeight;

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getBattleFieldHeight() {
        return bHeight;
    }

    @Override
    public double getBattleFieldWidth() {
        return bWidth;
    }

    @Override
    public void onPaint(Graphics2D g) {
        Renderable.onPaint(g);
        Renderable.clear();
    }

    public static void main(String[] args){
        Robocode.main(args);
    }
}
