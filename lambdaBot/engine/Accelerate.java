package jaara.engine;

import robocode.Rules;


// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.DDB696E9-9893-8489-D783-EF77D5781ACC]
// </editor-fold> 
public class Accelerate extends Action {

    private double value;
    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.1D9234EC-B167-6867-5FFC-FB700C19728C]
    // </editor-fold> 
    public Accelerate (double v) {
        value = v;
    }

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.95144716-0421-BFC2-90A1-571E7F715AAE]
    // </editor-fold> 
    public boolean isRunnable (World w) {
        double currentVelocity = w.getDesiredVelocity();

        if( (value < 0.0 && currentVelocity <= -Rules.MAX_VELOCITY) ||
            (value > 0.0 && currentVelocity >= Rules.MAX_VELOCITY))
            return false;

        return true;
    }

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.07876779-A8B4-6AA1-C316-95A77B5F1BF7]
    // </editor-fold> 
    public World run (World w) {
        World newWorld = w.clone();
        newWorld.accelerateMe(value);

        return newWorld;
    }

    @Override
    public void runReally() {
        double newVelocity = Engine.getRobot().getMaxVelocity() + value;

        Engine.getRobot().setMaxVelocity(newVelocity);
    }

    @Override
    public String toString() {
        return "Accelerate: "+value;
    }



}

