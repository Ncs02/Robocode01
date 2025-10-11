package jaara.engine;

import robocode.Rules;


// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.050A3648-8F72-F443-BC62-113ED40F99FF]
// </editor-fold> 
public class Steer extends Action {

    private double angle;
    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.DCD48CD4-B83C-0811-3F74-EDC898E7AB4D]
    // </editor-fold> 
    public Steer (double v) {
        this.angle = v;
    }

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.393B72F6-53A5-5802-9A4E-D7EA895893CF]
    // </editor-fold> 
    public World run (World w) {
        World newWorld = w.clone();
        newWorld.turnMe(angle);

        return newWorld;
    }

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.8B239D4D-FC7E-9271-3E9D-E36015D93971]
    // </editor-fold> 
    public boolean isRunnable (World w) {
        return !w.isSteering();
    }

    @Override
    public void runReally() {
        /*if(angle < Rules.MAX_TURN_RATE_RADIANS){
            Engine.robot.setTurnRightRadians(angle);
        }
        else{
            if(angle > 0){
                Engine.robot.setTurnRightRadians(-Rules.MAX_TURN_RATE_RADIANS);
            }
            else{
                Engine.robot.setTurnRightRadians(-Rules.MAX_TURN_RATE_RADIANS);
            }
        }*/

        Engine.getRobot().setTurnRightRadians(angle);
    }

    @Override
    public String toString() {
        return "Steer: "+angle;
    }



}

