import java.awt.*;

/*
    Bug in java.awt.Polygon:

    void updateBounds(int x, int y) {
	bounds.x = Math.min(bounds.x, x);
	bounds.width = Math.max(bounds.width, x - bounds.x);
	bounds.y = Math.min(bounds.y, y);
	bounds.height = Math.max(bounds.height, y - bounds.y);
    }
*/
public class Polybug extends java.applet.Applet {
    static String f() {
	Polygon p = new Polygon();
	p.addPoint(0, 100);
	p.addPoint(100, 100);
	p.addPoint(100, 60);
	p.addPoint(50, 0); // updateBounds NOT called so far
	return p.inside(50,50)? "inside" : "outside";
    }
    static String g() {
	Polygon p = new Polygon();
	p.addPoint(0, 100);
	p.addPoint(100, 100);
        p.addPoint(100, 60);
        p.inside(0,0); // this will cause updateBounds to be called
        p.addPoint(50, 0); // updateBounds called now
        return p.inside(50,50)? "inside" : "outside";
    }
    public static void main(String args[]) {
	System.out.println("correct result: inside");
	System.out.println("f(): "+f());
	System.out.println("g(): "+g());
    }
    public void paint(java.awt.Graphics g) {
	Polygon p = new Polygon();
	p.addPoint(0, 100);
	p.addPoint(100, 100);
        p.addPoint(100, 60);
        p.addPoint(50, 0); // updateBounds called now

	// draw polygon, print results
	g.setColor(Color.red);
	g.fillPolygon(p);
	g.setColor(Color.black);
	g.drawLine(55,55,65,65);
	g.drawLine(55,65,65,55);
	g.drawString("f(): "+f(), 120,20);
	g.drawString("g(): "+g(), 120,50);
    }
}
