function show(newwin, set, i)
{
	var v = navigator.appVersion.substring(0, 2);
	if(v == "2.")
		return true;
	var links = picLinks;
	var widths = picW;
	var heights = picH;
	var titles = picTitle;
	var notes = picNote;
	if(newwin) {
		w = window.open("", "show", "width="+screen.availWidth+
				",height="+(screen.availHeight-10));
		w.moveTo(0, 0);
	} else {
		w = self;
	}
	var d = w.document;
	d.open();
	d.write('<html>\n<head>\n');
	d.write('<title>'+titles[set][i]+'</title>\n');
	d.write('</head>\n');
	d.write('<body BGCOLOR="#202020" TEXT="#c0c0ff">\n');
	d.write('<script LANGUAGE="JavaScript1.1">\n');
	d.write('picLinks = new Array(); picW = new Array(); picH = new Array();\n');
	d.write('picTitle = new Array(); picNote = new Array();\n');
	for(var j = 0; j < links.length; ++j) {
		d.write('picLinks['+j+'] = new Array(); picW['+j+'] = new Array(); picH['+j+'] = new Array();\n');
		d.write('picTitle['+j+'] = new Array(); picNote['+j+'] = new Array();\n');
		for(var k = 0; k < links[j].length; ++k) {
			d.write('picLinks['+j+']['+k+'] = "'+links[j][k]+'"; '+
				'picW['+j+']['+k+'] = '+widths[j][k]+'; '+
				'picH['+j+']['+k+'] = '+heights[j][k]+';\n');
			d.write('picTitle['+j+']['+k+'] = "'+titles[j][k]+'"; '+
				'picNote['+j+']['+k+'] = "'+notes[j][k]+'";\n');
		}
	}
	d.write('</script>\n');
	d.write('<script LANGUAGE="JavaScript1.1" SRC="show.js"></script>');
	d.write('<center>\n');
	var buttons = true || i > 0 || i < links[set].length - 1;
	if(buttons) {
		d.write('<form>\n');
	}
	if(i > 0) {
		d.write('<font COLOR="#333333">'+
			'<input TYPE=BUTTON VALUE="<<<" '+
			'onClick="show(false, '+set+', '+(i-1)+')"></font>\n');
	} else {
		d.write('<font COLOR="#202020">&lt;&lt;&lt;</font>\n');
	}
	d.write('<font COLOR="#333333">'+
		'<input TYPE=BUTTON VALUE="Close" '+
		'onClick="self.close()"></font>\n');
	if(!buttons)
		d.write('<br>\n');
	if(i < links[set].length - 1) {
		d.write('<font COLOR="#333333">'+
			'<input TYPE=BUTTON VALUE=">>>" '+
			'onClick="show(false, '+set+', '+(i+1)+')"></font>\n');
	} else {
		d.write('<font COLOR="#202020">&gt;&gt;&gt;</font>\n');
	}
	if(buttons) {
		d.write('</form>\n');
	}
	d.write('<img SRC='+links[set][i]);
	d.write(' WIDTH='+widths[set][i]);
	d.write(' HEIGHT='+heights[set][i]);
	d.write(' BORDER=0></center>\n');
	d.write('<p ALIGN=CENTER>'+titles[set][i]+'</p>\n');
	d.write('<p ALIGN=CENTER><font SIZE="-1">'+notes[set][i]+'</font></p>\n');
	d.write('<p ALIGN=RIGHT>'+(i+1)+'</p>\n');
	d.write('</body>\n');
	d.write('</html>\n');
	d.close();
	picLinks = links;
	picW = widths;
	picH = heights;
	picTitle = titles;
	picNote = notes;
	return false;
}
