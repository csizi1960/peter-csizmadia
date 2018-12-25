import java.awt.*;
import java.awt.event.*;
import java.util.Properties;
import javax.swing.*;

public class GetPrintJob extends JApplet implements ActionListener
{
    Frame frame = null;
    JButton button1;
    JButton button2;
    public void init() {
	getContentPane().add(new JLabel("click getPrintJob in the other window",
					JLabel.CENTER));
    }
    public void start() {
	JFrame f = new JFrame();
	Container p = f.getContentPane();
	p.setLayout(new GridLayout(3, 1));
	p.add(new JLabel("Click the button"));
	p.add(button1 = new JButton("getPrintJob"));
	button1.addActionListener(this);
	p.add(button2 = new JButton("getPrintJob + getGraphics"));
	button2.addActionListener(this);
	f.pack();
	f.setVisible(true);
	frame = f;
    }
    public void stop() {
	frame.dispose();
    }
    public void actionPerformed(ActionEvent ev) {
	Object src = ev.getSource();
	if(src == button1) {
	    getToolkit().getPrintJob(frame, "", new Properties());
	} else if(src == button2) {
	    PrintJob job = getToolkit().getPrintJob(frame, "", new Properties());
	    Graphics g = job.getGraphics();
	}
    }
}
