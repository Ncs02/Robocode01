
package jaara.waveSurfing;

import java.util.Arrays;

/**
 *
 * @author Jaromír Janisch <jaara.j@email.cz>
 */
public class Probability {
    private int segmentCount = 21;
    private int history = 5;

    private int[] lasts;
    private int counter=0;

    public Probability(int segments, int historyLength) {
        segmentCount = segments;
        history = historyLength;

        lasts = new int[history];
    }

    /**
     * Allows a new addition to the probability interval
     * @param f
     */
    public void add(double f){
        int s = computeSegment(f);

        lasts[counter++ % history] = s;
        
        change = true;
    }

    /**
     * Computes right segment for given value
     * @param f value
     * @return segment
     */
    private int computeSegment(double f){
        if(f < 0 && f > -0.05)
            f = 0.0;
        else if(f > 1 && f < 1.05)
            f = 1.0;

        if(f < 0 || f > 1)
            throw new IllegalArgumentException("Ilegal f: " + f);

        int s = (int) (f * segmentCount);
        if(s == segmentCount)
            s--;

        return s;
    }

    /**
     * Computes right value for given segment
     * @param s - segment
     * @return value <0,1>
     */
    private double decomputeSegment(int s){
        return (s+0.5)/segmentCount;
    }


    private double[] segmentCache;
    private boolean change = true;
    /**
     *
     * @return all internal computed segments
     */
    public double[] getSegments(){
        if(!change)
            return segmentCache;

        double[] segments = new double[segmentCount];

        double plus = 1.0 / (counter<history ? counter : history);
        for(int i=0; i<counter && i<history; i++){
            segments[lasts[i]] += plus;
        }

        segmentCache = segments;
        change = false;
        
        return segments;
    }
//    private int segNumDouble = 4;
//    public double[] getSegments(){
//        double[] segments = new double[segmentCount];
//
//        int minCH = counter<history ? counter : history;
//        int minCS = counter<segNumDouble ? counter : segNumDouble;
//        double plus = 1.0 / ( minCH + minCS);
//        for(int i=0; i<minCH; i++){
//            segments[lasts[i]] += plus;
//            if( i >= minCH - minCS){
//                segments[lasts[i]] += plus;
//            }
//        }
//
//        return segments;
//    }

    /**
     *
     * @return most probable value from interval <0,1>
     */
    public double getMostProbableSegment(){
        double[] segments = getSegments();

        double maxVal = -1;
        int maxSeg = 0;
        for(int i=0; i<segmentCount; i++){
            if(maxVal < segments[i]){
                maxVal = segments[i];
                maxSeg = i;
            }
        }

        return decomputeSegment(maxSeg);
    }

    /**
     * As robot space often contains more than one segment, this method returns probability integral over <min, max> interval
     * @param min
     * @param max
     * @return computed probablity
     */
    public double getProbability(double min, double max){
        int start = computeSegment(min);
        int end   = computeSegment(max);

        double[] segments = getSegments();

        double value = 0.0;
        for(int i=start; i<=end; i++)
            value += segments[i];

        return value;
    }

    @Override
    public String toString() {
        return Arrays.toString(getSegments());
    }
//
//    public static void main(String[] args){
//        Probability p = new Probability();
//
//        p.add(1.0);
//        p.add(0.0);
//        p.add(1.0);
//        p.add(0.5);
//        p.add(1.0);
//        p.add(1.0);
//        p.add(1.0);
//        p.add(1.0);
//        p.add(1.0);
//
//        System.out.println(Arrays.toString(p.getSegments()));
//    }
}
