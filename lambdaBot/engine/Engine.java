package jaara.engine;

import jaara.LambdaBot;
import java.util.*;

/**
 *
 * @author Jaromír Janisch <jaara.j@email.cz>
 */
public class Engine {
    private static LambdaBot robot;

    public static LambdaBot getRobot() {
        return robot;
    }

    private static final int TURNS_PER_TICK = 10; //pocet kol v kroku
    private static final int MAX_DEPTH = 4; //maximalni hloubka
    private static final int FULL_DEPTH = 0; //hloubka do ktere bude strom prohledan cely
    private static final int EXPAND_NODES_PER_TICK = 1; //pocet uzlu, ktere budou rozsireny v jednom kroku
    private static final int OPENLIST_CAP = 20000; //pokud je seznam vetsi, nez tato hodnota
    private static final int CAP_EXPAND = 500; //je redukovan na tuto hodnotu
    
    private static ArrayList<Action> actionList = new ArrayList<Action>();

    static {
        //initialize the aciton list

//        actionList.add(new Steer(Math.PI));
        actionList.add(new Steer(Math.PI/2.0));
        actionList.add(new Steer(Math.PI/4.0));
        actionList.add(new Steer(Math.PI/8.0));
//        actionList.add(new Steer(Rules.MAX_TURN_RATE_RADIANS * TURNS_PER_TICK / 8.0));

//        actionList.add(new Steer(-Math.PI));
        actionList.add(new Steer(-Math.PI/2.0));
        actionList.add(new Steer(-Math.PI/4.0));
        actionList.add(new Steer(-Math.PI/8.0));
//        actionList.add(new Steer(-Rules.MAX_TURN_RATE_RADIANS * TURNS_PER_TICK / 8.0));

        actionList.add(new Accelerate(8.0));
        actionList.add(new Accelerate(4.0));
        actionList.add(new Accelerate(2.0));
        actionList.add(new Accelerate(1.0));

        actionList.add(new Accelerate(-8.0));
        actionList.add(new Accelerate(-4.0));
        actionList.add(new Accelerate(-2.0));
        actionList.add(new Accelerate(-1.0));

        actionList.add(new Stop());
        actionList.add(new NoAction());
    }

    public Engine(LambdaBot r) {
        robot = r;
    }

    /**
     * Envelope for World and Action classes
     */
    private class Node {
        public Action action;
        public World world;

        public double value;
        public int depth;
        public Node previous;

        public Node(Action action, World world, Node previous, double value, int depth) {
            this.action = action;
            this.world = world;
            this.previous = previous;
            this.value = value;
            this.depth = depth;
        }

        @Override
        public int hashCode() {
//            return (int) (value / 10);
            return world.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof Node))
                return false;
            Node n = (Node)obj;

//            if(Math.abs(n.value-value) > 1.0)
//                return false;

            return world.equals(n.world);
        }


    }

    /**
     * Expands a node
     * @param n node to expand
     */
    private void expandNode(Node n){
        if(n.depth >= MAX_DEPTH)
            return;

        for( Action a : actionList ){
            if(a.isRunnable(n.world)){
                World newWorld = a.run(n.world);
                newWorld.run(TURNS_PER_TICK);

                //System.out.println(""+newWorld.getMe().getPosition().x+" : "+evaluateWorld(newWorld));
                double worldValue = evaluateWorld(newWorld);
                Node c = new Node(a, newWorld, n, worldValue, n.depth+1); //create a new node with altered world

                if(closedList.contains(c))
                    continue;
                
                openList.add(c);
            }
        }
    }

    private double evaluateWorld(World w){
        return w.evaluate();
    }


    private World rootWorld = null;

    /**
     * Input function for new root node
     * @param w new root state
     */
    public void updateWorld(World w){
        rootWorld = w;
    }

    Collection<Node> openList;
    Collection<Node> closedList;


    /**
     * Finds the best node
     * @return best node
     */
    private Node getBestNode(){
        if(openList.isEmpty())
            return null;

        Node best = null;

        for(Node n : openList){
            if(best == null){
                best = n;
                continue;
            }

            //System.out.println("MyX: " + best.world.getMe().getPosition().x+" BestX: "+ best.value);
            if(n.value < best.value){
                best = n;
            }
        }

        return best;
    }

    /**
     * Main entry point
     */
    public void run(){
        if(rootWorld == null)
            return;

        openList = new LinkedHashSet<Node>();
        closedList = new HashSet<Node>();
        
        Node root = new Node( new NoAction(), rootWorld, null, evaluateWorld(rootWorld), 0 );

//        System.out.println("W: "+evaluateWorld(rootWorld));
        openList.add(root);

        for(int i=0; i<FULL_DEPTH; i++){ //for first time, expand all nodes to defined depth
            Collection<Node> oldList = openList;
            openList = new LinkedHashSet<Node>();

            for(Node n : oldList){
                expandNode(n);
                closedList.add(n);
            }
        }

        ArrayList<Node> bestNodes = null;
        while(true){
            if(openList.isEmpty())
                break;

            bestNodes = new ArrayList<Node>(EXPAND_NODES_PER_TICK);

            if(openList.size() > OPENLIST_CAP){
                LinkedHashSet<Node> newList = new LinkedHashSet<Node>();
                for(int i=0; i<CAP_EXPAND; i++){
                    Node b = getBestNode();
                    newList.add(b);
                    openList.remove(b);
                }

                openList = newList;

                System.out.println("TREE TOO BIG!");
            }

            for(int i=0; i<EXPAND_NODES_PER_TICK; i++){
                Node n = getBestNode();
                
                openList.remove(n);
                closedList.add(n);

                bestNodes.add(n);
            }

            if(bestNodes.get(0).depth >= MAX_DEPTH) //the most promising node
                break;

            for(Node n: bestNodes){
                expandNode(n);
            }
        }        

        //trace back
        Node best = bestNodes.get(0);

        while(best.previous.previous != null){
            best = best.previous;
        }

        best.action.runReally();
//        System.out.println("Executing: " + best.action.toString() );
    }


}