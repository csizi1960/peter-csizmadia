import java.awt.*;

public class ParamBug extends java.applet.Applet
{
    TextArea txt;
    public void init() {
	setLayout(new GridLayout(1, 1));
	add(txt = new TextArea());
	txt.setFont(new Font("Monospaced", Font.PLAIN, 12));

	for(char i = 'a'; i < 'z'; ++i) {
	    String s = getParameter(String.valueOf(i));
	    if(s != null)
		txt.appendText(s+"\n");
	}
    }
}
