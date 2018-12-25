import java.awt.*;

public class Scrollbug extends java.applet.Applet {
    public void init() {
	setLayout(new GridLayout(1,1));
	add(new Scrollbar(Scrollbar.HORIZONTAL, 0, 50, 0, 50));
    }
}
