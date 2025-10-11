package jaara.engine;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;


// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.570C681E-978C-6885-8E34-C70109D0619F]
// </editor-fold> 
public class BotState {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.7B39447E-413A-7DAA-D392-55A636B3E460]
    // </editor-fold> 
    private Point2D.Double position;

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.4EAB01A2-9CE5-866B-B075-099656246238]
    // </editor-fold> 
    private double life;

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.ABA8B2D3-DB16-79A9-6E68-91869ED9DDB0]
    // </editor-fold> 
    private double velocity;

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.9E83E392-3988-E049-EB39-55A4B8ADF42A]
    // </editor-fold> 
    private double heading;

    public BotState(Double position, double life, double velocity, double heading) {
        this.position = position;
        this.life = life;
        this.velocity = velocity;
        this.heading = heading;
    }

    @Override
    protected BotState clone(){
        BotState n = new BotState(new Point2D.Double(position.x, position.y), life, velocity, heading);

        return n;
    }

    public double getHeading() {
        return heading;
    }

    public double getLife() {
        return life;
    }

    public Point2D.Double getPosition() {
        return position;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    
    public void setLife(double life) {
        this.life = life;
    }

    public void setPosition(Double position) {
        this.position = position;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof BotState))
            return false;

        BotState b = (BotState)obj;

        if( Math.abs(position.x-b.position.x) < 1 && Math.abs(position.y-b.position.y) < 1 &&
                Math.abs(life-b.life) < 1 &&
                Math.abs(velocity-b.velocity) < 1 && Math.abs(heading-b.heading) < 0.1 )
            return true;

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash += position.x / 2;
        hash *= 100;
        hash += position.y / 2;
        hash *= 10;
        hash += life / 2;
        hash *= 10;
        hash += velocity / 2;
        hash *= 10;
        hash += heading * 5;

        return hash;
    }



}

