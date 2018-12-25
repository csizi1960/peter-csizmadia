import java.awt.*;
import java.applet.*;

public class Fontsizes extends Applet
{
    String name = null;
    public void init() {
	if(name == null)
	    name = getParameter("name");
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	setLayout(gbl);
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.weightx = 1;
	gbc.weighty = 1;
	gbc.gridwidth = 8;
	Label l = new Label(System.getProperty("java.vendor"));
	l.setFont(new Font(name, Font.PLAIN, 12));
	gbl.setConstraints(l, gbc);
	add(l);
	gbc.gridwidth = 1;
	for(int size=1; size<41; ++size) {
	    l = new Label(String.valueOf(size));
	    l.setFont(new Font(name, Font.PLAIN, size));
	    gbc.gridx = (size-1)%8;
	    gbc.gridy = 1+(size-1)/8;
	    gbl.setConstraints(l, gbc);
	    add(l);
	}
    }
    public static void main(String[] args) {
	Frame f = new Frame();
	Fontsizes p = new Fontsizes();
	p.name = args[0];
	p.init();
	f.setLayout(new GridLayout(1,1));
	f.add(p);
	f.pack();
	f.setVisible(true);
    }
}
