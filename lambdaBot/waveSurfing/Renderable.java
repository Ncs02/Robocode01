
package jaara.waveSurfing;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;

/**
 *
 * @author Jaromír Janisch <jaara.j@email.cz>
 */
public abstract class Renderable{
    public abstract void render(Graphics2D g);

    private static Collection<Renderable> _renderables = new LinkedList<Renderable>();

    public static void drawLine(Point2D.Double p1, Point2D.Double p2, Color color){
        _renderables.add(new Renderable.Line(p1, p2, color));
    }

    public static void drawCircle(Point2D.Double center, double radius, Color color){
        _renderables.add(new Renderable.Circle(center, radius, color));
    }

    public static void drawPoint(Point2D.Double p1, Color color){
        _renderables.add(new Renderable.Dot(p1, color));
    }

    public static void drawText(String text, double x, double y, Color color){
        _renderables.add(new Renderable.Text(text, x, y, color));
    }

    public static void clear(){
        _renderables.clear();
    }

    public static void onPaint(Graphics2D g){
        for(Renderable r:_renderables)
            r.render(g);
    }

    public static class Circle extends Renderable{
        Point2D.Double center;
        double radius;
        Color color;
        public Circle(Point2D.Double center, double radius, Color color){
            this.center = center;
            this.radius = radius;
            this.color = color;
        }
        public void render(Graphics2D g) {
            g.setColor(color);
            g.drawOval(	(int)Math.round(center.x - radius),
                        (int)Math.round(center.y - radius),
                        (int)Math.round(2 * radius),
                        (int)Math.round(2 * radius));
        }
    }

    public static class Dot extends Renderable{
        Point2D.Double point;
        double radius;
        Color color;
        public Dot(Point2D.Double point, Color color){
            this.point = point;
            this.radius = 2;
            this.color = color;
        }
        public void render(Graphics2D g) {
            g.setColor(color);
            g.fillOval(	(int)Math.round(point.x - radius),
                        (int)Math.round(point.y - radius),
                        (int)Math.round(2 * radius),
                        (int)Math.round(2 * radius));
        }
    }

    public static class Line extends Renderable{
        Point2D.Double p1, p2;
        Color color;

        double radius;
        public Line(Point2D.Double p1, Point2D.Double p2, Color color){
            this.p1 = p1;
            this.p2 = p2;
            this.color = color;
        }
        public void render(Graphics2D g) {
            g.setColor(color);
            g.drawLine(	(int)Math.round(p1.x),
                        (int)Math.round(p1.y),
                        (int)Math.round(p2.x),
                        (int)Math.round(p2.y));
        }
    }

    public static class Text extends Renderable{
        String text;
        double x, y;
        Color color;

        double radius;
        public Text(String text, double x, double y, Color color){
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
        }
        public void render(Graphics2D g) {
            g.setColor(color);
            g.drawString(text, (float)x, (float)y);
        }
    }
}

