
package jaara.waveSurfing;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import robocode.Rules;

/**
 *
 * @author Jaromír Janisch <jaara.j@email.cz>
 */
public class Wave {
    private Point2D.Double origin;
    private double power;
    private double bearing;
    private long fireTime;

    public Double getOrigin() {
        return origin;
    }

    public double getSpeed() {
        return Rules.getBulletSpeed(power);
    }

    public double getPower() {
        return power;
    }

    public double getBearing() {
        return bearing;
    }

    public long getFireTime() {
        return fireTime;
    }


    public Wave(Double origin, double firePower, double bearing, long fireTime) {
        this.origin = origin;
        this.power = firePower;
        this.bearing = bearing;
        this.fireTime = fireTime;
    }

    public Wave(Double origin, double power, double bearing, long fireTime, double linarGunAngle) {
        this.origin = origin;
        this.power = power;
        this.bearing = bearing;
        this.fireTime = fireTime;
        this.linarGunAngle = linarGunAngle;
    }
    
    public double getDistance(long time){
        return getSpeed() * (time - fireTime);
    }

    private double linarGunAngle;

    public double getLinarGunAngle() {
        return linarGunAngle;
    }
}
