import java.awt.*;

public class GridBug128 extends java.applet.Applet {
    public void init() {
	Color[] colors = {Color.white, Color.lightGray, Color.gray,
			  Color.darkGray, Color.black, Color.red,
			  Color.yellow, Color.green, Color.cyan, Color.blue};
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	setLayout(gbl);
	gbc.gridx = 0;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = gbc.weighty = 1.0;
	for(int i=0; i<129; ++i) {
	    Canvas can = new Canvas();
	    can.setBackground(colors[i%10]);
	    gbc.gridy = i;
	    gbl.setConstraints(can, gbc);
	    add(can);
	}
    }
}
