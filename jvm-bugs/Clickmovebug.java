import java.awt.*;

public class Clickmovebug extends java.applet.Applet {
    long bwhen;
    int bx, by;
    Canvas cnv = new Canvas();
    TextArea txt = new TextArea();
    public void init() {
	cnv.setBackground(new Color(255,0,0));
	txt.setFont(new Font("Courier", Font.PLAIN, 12));
	setLayout(new GridLayout(1,2));
	add(cnv);
	add(txt);
    }
    public boolean mouseDown(Event e, int x, int y) {
	log("mouseDown", e);
	return false;
    }
    public boolean mouseUp(Event e, int x, int y) {
	log("mouseUp", e);
	return false;
    }
    public boolean mouseEnter(Event e, int x, int y) {
	log("mouseEnter", e);
	return false;
    }
    public boolean mouseExit(Event e, int x, int y) {
	log("mouseExit", e);
	return false;
    }
    public boolean mouseMove(Event e, int x, int y) {
	log("mouseMove", e);
	return false;
    }
    public boolean mouseDrag(Event e, int x, int y) {
	log("mouseDrag", e);
	return false;
    }
    public void log(String m, Event e) {
	if(e.target == cnv) {
	    bwhen = e.when;
	    bx = e.x;
	    by = e.y;
	    String s = msg(m, e.x, e.y, e.when);
//	    System.out.print(s);
	    txt.appendText(s);
	}
    }
/*    public void log2(String m, Event e) {
	if(e.target == cnv) {
	    String s = msg(m, e.x, e.y, e.when);
	    System.out.print(s);
	    if(bx == e.x && by == e.y)
		txt.appendText(s);
	}
    }*/
    public String msg(String m, int x, int y, long when) {
	StringBuffer s = new StringBuffer(m);
	s.append(" x=");
	s.append(x);
	s.append(" y=");
	s.append(y);
	s.append(" when=");
	s.append(when);
	s.append('\n');
	return s.toString();
    }
}
