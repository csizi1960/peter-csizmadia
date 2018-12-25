import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * Calculator GUI.
 * @version 03/03/2008
 * @since 05/02/2006
 * @author Peter Csizmadia
 */
public class CalculatorGUI extends JApplet {
    private static final int UNITS_SI = 0;
    private static final int UNITS_NUCLEAR = 2;
    private static final int UNITS_PLANCK = 1;
    private static final String HELP = Calculator.getHelpSI()+"\n" +
"Systems of units: SI, nuclear, Planck (default: SI)\n" +
"? or help - type this help, ?X - help for symbol X\n";
    private int units = UNITS_SI;
    private JTextArea textArea;
    public CalculatorGUI() {
	Color bg = Color.black;
	Color fg = Color.white;
	setBackground(bg);
	setForeground(fg);

	JScrollPane pane = new JScrollPane(textArea = new JTextArea(),
		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	add(pane);

	textArea.setBackground(bg);
	textArea.setForeground(fg);
	textArea.setCaretColor(Color.yellow);

	textArea.setText("Calculator / 1992.05.24(realfunc.cpp)-2009.08.06\n\n" + HELP + "\n");
	textArea.setLineWrap(true);
	textArea.setCaretPosition(textArea.getText().length());
	textArea.addKeyListener(new KeyAdapter() {
	    public void keyPressed(KeyEvent ev) {
		if(ev.getKeyChar() == KeyEvent.VK_ENTER) {
		    int i = textArea.getCaretPosition();
		    int l = 0;
		    int start = 0;
		    int end = 0;
		    try {
			l = textArea.getLineOfOffset(i);
			start = textArea.getLineStartOffset(l);
			end = textArea.getLineEndOffset(l);
			textArea.setCaretPosition(end);
			String line = textArea.getDocument().getText(start,
				end - start);
			String res = eval(line);
			if(l == textArea.getLineCount() - 1) {
			    textArea.append("\n" + res);
			} else {
			    int end2 = textArea.getLineEndOffset(l + 1);
			    textArea.replaceRange(res, end, end2);
			}
		    } catch(BadLocationException ex) {
			textArea.append(ex.getMessage());
			ex.printStackTrace();
		    }
		}
	    }
	});
	setLayout(new GridLayout(1, 1));
    }

    private String eval(String line) {
	line = line.trim();
	if(line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")
		|| line.equals("bye") || line.equals("q")) {
	    System.exit(0);
	}
	if(line.equals("help") || line.equals("?")) {
	    return HELP;
	} else if(line.startsWith("?")) {
	    String what = line.substring(1).trim();
	    String help = Calculator.getHelp(what);
	    return (help != null)? help : ("no help for " + what);
	} else if(line.equals("SI")) {
	    units = UNITS_SI;
	    return "<<< using SI units >>>";
	} else if(line.equals("nuclear")) {
	    units = UNITS_NUCLEAR;
	    return "<<< using nuclear units (c = hbar = GeV = 1) >>>";
	} else if(line.equals("Planck")) {
	    units = UNITS_PLANCK;
	    return "<<< using Planck units (c = hbar = G = 1) >>>";
	}
	try {
	    Calculator c = null;
	    if(units == UNITS_SI) {
		c = Calculator.createSI(line);
	    } else if(units == UNITS_NUCLEAR) {
		c = Calculator.createNuclear(line);
	    } else if(units == UNITS_PLANCK) {
		c = Calculator.createPlanck(line);
	    }
	    return String.valueOf(c.eval());
	} catch(IllegalArgumentException ex) {
	    return ex.getMessage();
	} catch(ArrayIndexOutOfBoundsException ex) {
	    return "parse error";
	}
    }

    public static void main(String[] args) {
	final JFrame f = new JFrame();
	final JApplet applet = new CalculatorGUI();
	f.setTitle("Calculator");
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	f.setContentPane(applet);
	f.pack();
	f.setSize(new Dimension(640, 400));
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		f.setVisible(true);
	    }
	});
    }
}
