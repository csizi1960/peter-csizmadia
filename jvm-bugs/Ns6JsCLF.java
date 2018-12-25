import java.awt.*;
import java.io.*;

public class Ns6JsCLF extends java.applet.Applet
{
    private TextArea txt;

    public void init() {
	setLayout(new GridLayout(1, 1));
	add(txt = new TextArea());
    }

    /**
     * Method containing Class.forName.
     * When it is called from JavaScript, Netscape 6/Sun Java 1.3 freezes.
     */
    public void load(String s) {
	try {
	    Class cl = Class.forName(s);
	    Object o = cl.newInstance();
	    txt.appendText(o.toString());
	    txt.appendText("\n\n*** no freezing ***");
	} catch(Exception ex) {
	    // print exception in the text area
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    PrintStream ps = new PrintStream(baos);
	    ex.printStackTrace(ps);
	    byte[] bytes = baos.toByteArray();
	    StringBuffer sbuf = new StringBuffer();
	    for(int i = 0; i < bytes.length; ++i) {
		sbuf.append(String.valueOf((char)bytes[i]));
	    }
	    txt.appendText(sbuf.toString());
	}
    }
}
