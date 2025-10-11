package jaara.engine;


// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.D117410D-01B8-20A3-9959-BA5E55D43858]
// </editor-fold> 
public class NoAction extends Action {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.5E7F2E1A-1E84-7A56-3C58-0F627C849116]
    // </editor-fold> 
    public World run (World w) {
        World newWorld = w.clone();

        return newWorld;
    }

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.C056E3C4-BA4B-0506-C7DA-5EE446D83C67]
    // </editor-fold> 
    public boolean isRunnable (World w) {
        return true;
    }

    @Override
    public void runReally() { }

    @Override
    public String toString() {
        return "No action";
    }


}

