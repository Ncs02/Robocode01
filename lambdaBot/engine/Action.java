package jaara.engine;


// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.14FB9122-92CC-9710-0C15-68118D6B6311]
// </editor-fold> 
public abstract class Action {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.15647ABA-45FF-570B-D7F8-19941D3BFC13]
    // </editor-fold> 
    public abstract World run (World w);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.E3F076B6-46F9-6C68-3890-1B9940EE8DD6]
    // </editor-fold> 
    public abstract boolean isRunnable (World w);

    public abstract void runReally();
}

