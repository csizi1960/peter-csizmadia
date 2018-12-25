import java.awt.*;

public class Jitdag extends java.applet.Applet {
    double a;
    double b = 6;
    public void init() {
	int bug = Integer.parseInt(getParameter("bug"));
	double c;
	double e = 3;
	double f = 4;
	boolean d = (System.currentTimeMillis()&1) == 1;
	if(bug == 0) {
	    a = b*(c=d?e:f);
	} else {
	    method(c=d?e:f);
	}
	System.out.println("a="+a);
    }
    void method(double x) {
	a = b*x;
    }
    public void paint(Graphics g) {
	g.drawString("a="+a, 10, 10);
	g.drawString("see Java Console", 10, 50);
    }
}
