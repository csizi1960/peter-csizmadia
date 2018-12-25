import java.awt.*;
import java.awt.event.*;

public class PopupCheckbox extends java.applet.Applet implements
    MouseListener, ItemListener, ActionListener
{
    PopupMenu popup;
    int checkCount = 0;
    int clickCount = 0;
    String message = "Right click here!";

    public void init() {
	CheckboxMenuItem cb;
	MenuItem mi;
	addMouseListener(this);
	add(popup = new PopupMenu());

	popup.add(cb = new CheckboxMenuItem("checkme!"));
	cb.addItemListener(this);

	popup.add(mi = new MenuItem("clickme!"));
	mi.addActionListener(this);

	Menu submenu = new Menu("submenu");
	popup.add(submenu);

	submenu.add(cb = new CheckboxMenuItem("checkme!"));
	cb.addItemListener(this);

	submenu.add(mi = new MenuItem("clickme!"));
	mi.addActionListener(this);
    }
    public void paint(Graphics g) {
	Dimension d = getSize();
	FontMetrics fm = g.getFontMetrics();
	g.drawString(message, (d.width - fm.stringWidth(message))/2, d.height/2);
    }
    public void itemStateChanged(ItemEvent ev) {
	Object src = ev.getSource();
	System.out.println("itemStateChanged: "+src);
	CheckboxMenuItem mi = (CheckboxMenuItem)src;
	mi.setLabel(mi.getState()? "uncheckme!" : "checkme!");
	++checkCount;
	message = "CheckboxMenuItem (un)checked";
	if(checkCount > 1)
	    message += " "+checkCount+" times";
	repaint();
    }
    public void actionPerformed(ActionEvent ev) {
	Object src = ev.getSource();
	System.out.println("actionPerformed: "+src);
	++clickCount;
	message = "MenuItem clicked";
	if(clickCount > 1)
	    message += " "+clickCount+" times";
	repaint();
    }
    public void mousePressed(MouseEvent ev) {
	if(ev.isPopupTrigger())
	    popup.show(this, ev.getX(), ev.getY());
    }
    public void mouseReleased(MouseEvent ev) {
	if(ev.isPopupTrigger())
	    popup.show(this, ev.getX(), ev.getY());
    }
    public void mouseClicked(MouseEvent ev) { }
    public void mouseExited(MouseEvent ev) { }
    public void mouseEntered(MouseEvent ev) { }
}
