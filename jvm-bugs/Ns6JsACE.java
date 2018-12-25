import java.awt.*;
import java.io.*;
import java.net.*;

public class Ns6JsACE extends java.applet.Applet
{
    private TextArea txt;

    public void init() {
	setLayout(new GridLayout(1, 1));
	add(txt = new TextArea());
    }

    /**
     * Method containing I/O operations.
     * When it is called from JavaScript, Netscape 6/Sun Java 1.3 throws an
     * AccessControlException.
     */
    public void load(String s) {
	try {
	    URL url = new URL(getDocumentBase(), s);
	    URLConnection conn = url.openConnection();
	    InputStream in = conn.getInputStream();
	    int c;
	    while((c = in.read()) != -1) {
		txt.appendText(String.valueOf((char)c));
	    }
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
