#!/usr/bin/perl -w

use Image::Magick;

open(INDEXHTML, ">index.html");

# make the first part of index.html
begin_indexhtml();

# make initial empty image
my $p = new Image::Magick;
my $indexwidth = 470;
my $indexheight = 604;
my $font = "-adobe-helvetica-medium-r-normal--10-*-*-*-p-*-iso8859-2";
$p->Set(size=>"$indexwidth"."x"."$indexheight");
$p->Read("xc:#202040");
print INDEXHTML <<EOF;
<img SRC="index.jpg" WIDTH="$indexwidth" HEIGHT="$indexheight" BORDER=0 USEMAP="#imgmap" ALT="Click for large image">
<script LANGUAGE="JavaScript1.1">
<!--
picLinks = new Array(); picW = new Array(); picH = new Array();
picTitle = new Array(); picNote = new Array();
EOF

# create image object for thumbnails
my $q = new Image::Magick;

my $absIndex = 0;
my @picL;
my @picT;
my @picR;
my @picB;
my @picLinks;
my @picSetI;

my $yoff = 0;
my $dytxt = 10;

# Grossglockner (2002)
printf STDERR "Grossglockner (2002)\n";
$p->Annotate(font=>$font, fill=>"#c0c0ff",
	geometry=>"200x16+80+".($yoff+$dytxt+45),
	align=>Left,
	text=>"Großglockner (3798)\n2002. aug. 18-19.");
addpics(10, 0, $yoff, 0,
	"2002glockner-studlgrat.jpg", "42x62|0|0|St&uuml;dlgrat (III, III+)|64",
	"2002glockner-csucs.jpg", "38x62|42|0|3798m|64",
	"2002glockner-erzjh-naple.jpg", "62x42|80|0|A Nap (is) le|64|Erzherzog Johann H&uuml;tte, 3454m",
	"2002glockner-moonshine.jpg", "61x42|142|0|Moonshine|64",
	"2002glockner-studlgrat-le.jpg", "42x62|203|0|St&uuml;dlgrat &uacute;jra (le)|64");

# Liechtenstein (2002)
printf STDERR "Liechtenstein (2002)\n";
$p->Annotate(font=>$font, fill=>"#c0c0ff",
	geometry=>"200x16+260+".($yoff+$dytxt+45),
	align=>Left,
	text=>"Liechtenstein  2002. július");
addpics(9, 260, $yoff, 0,
	"2002li-gerinc.jpg", "61x43|0|0|Gerinc|64",
	"2002li-schwarzhorn-le.jpg", "61x43|61|0|Schwarzhornr&oacute;l visszan&aacute;zve|64",
	"2002li-schwarzhorn-grauspitz.jpg", "43x61|122|0|Grauspitz (Schwarzhornról)|64",
	"2002li-flowerfield.jpg", "43x61|165|0|N&ouml;v&eacute;nyzet|64");

# Monte Rosa Dufourspitze (2001)
printf STDERR "Monte Rosa Dufourspitze (2001)\n";
$yoff += 74;
$p->Annotate(font=>$font, fill=>"#c0c0ff",
	geometry=>"200x16+0+".($yoff+$dytxt+45),
	align=>Left,
	text=>"Monte Rosa\nDufourspitze (4634)\n2001.aug.1.");
addpics(8, 0, $yoff, 0,
#	"2001monterosa-kecskes.jpg", "61x42|0|0|Kecskék|64",
#	"2001monterosa-tobanfekve.jpg", "57x45|61|0|Tengerszemben fekszek|64",
	"2001monterosa+tengerszem.jpg", "59x44|0|0|Tengerszem|64",
	"2001monterosa-boulder.jpg", "61x43|59|0|H&aacute;ton nehez&iacute;tett &aacute;thajl&aacute;s|64|(fot&oacute;: Bal&aacute;zs)",
	"2001monterosa-vizespaltni.jpg", "43x61|120|0|Spaltniba patak zik|64",
	"2001monterosa-nagyko+matterhorn.jpg", "64x42|163|14|Mars|64",
	"2001monterosa-spaltni+matterhorn.jpg", "61x43|227|6|Spaltni|64",
	"2001monterosa-olaszo.jpg", "61x42|286|7|Olasz oldal|64",
	"2001monterosa-dufour-matterhorn.jpg", "62x42|347|7|Visszan&eacute;zve|64",
	"2001monterosa-tabla.jpg", "53x49|409|0|T&aacute;bla|64|(fot&oacute;: Bal&aacute;zs)",
	"2001monterosa-csucson.jpg", "59x44|409|49|Cs&uacute;csk&eacute;p|64|(fot&oacute;s: ismeretlen)",
	"2001monterosa-grenzgipfel.jpg", "62x42|347|49|Grenzgipfel (4618)|64",
	"2001monterosa-egyhegy.jpg", "61x44|286|49|Egy hegy|64",
	"2001monterosa-spaltnik.jpg", "61x43|223|81|Erre nem jutunk haza|64",
	"2001monterosa-kovespaltni.jpg", "61x44|162|80|Ink&aacute;bb a nedves szikla|64",
	"2001monterosa-bazispaltni.jpg", "42x63|120|61|Keresztmetszet|64",
	"2001monterosa-alsogleccser.jpg", "65x40|55|84|Als&oacute; gleccser|64"
);

# Nyári tanfolyam, Magas-Tátra, Kistarpataki völgy, Téry ház (2000)
printf STDERR "Nyari tanfolyam, Magas-Tatra, Kistarpataki volgy, Tery haz (2000)\n";
$yoff += 130;
$p->Annotate(font=>$font, fill=>"#c0c0ff",
	geometry=>"200x16+0+".($yoff+$dytxt+45),
	align=>Left, text=>"Magas-Tátra, 2000. június");
addpics(7, 0, $yoff, 0,
	"2000tatra-to.jpg", "59x43|0|0|T&oacute;|64",
	"2000tatra-vilagvege1.jpg", "68x38|59|0|K&ouml;d el&#337;ttem|64",
	"2000tatra-vilagvege2.jpg", "60x43|127|0|Vil&aacute;g v&eacute;ge|64",
	"2000tatra-felhbir.jpg", "56x46|187|0|Felh&#337;k birodalma|64",
	"2000tatra-jegcsapos.jpg", "39x55|230|0|J&eacute;gcsapos &uacute;t|64",
	"2000tatra-arnyak.jpg", "43x59|269|0|&Aacute;rnyak|64",
	"2000tatra-ugras.jpg", "60x47|312|0|Ugr&aacute;s|64|(fot&oacute;: Bubba)",
	"2000tatra-vorosle.jpg", "60x43|312|47|Lefel&eacute;|64",
	"2000tatra-terminator.jpg", "46x57|372|0||64|(fot&oacute;: Ilony)",
	"2000tatra-csucson2.jpg", "60x45|372|57|A V&ouml;r&ouml;storony cs&uacute;cs&aacute;n|64|(fot&oacute;: Csaba)",
	"2000tatra-motyka.jpg", "60x47|372|102|Bubba &mdash; Motyka (V)|64",
	"2000tatra-vorostor.jpg", "55x47|377|149|V&ouml;r&ouml;storony/Siroka veza (2461)|64"
);

# dachsteini képek (1999)
printf STDERR "Dachstein (1999)\n";
$yoff += 70;
$p->Annotate(font=>$font, fill=>"#c0c0ff",
	geometry=>"200x16+56+".($yoff+$dytxt+4),
	align=>Left,text=>"Dachstein, 1999.április");
addpics(6, 0, $yoff, 0,
	"1999dachst-madar.jpg", "39x66|0|0|Mad&aacute;r|64",
	"1999dachst-ufo.jpg", "61x42|39|24|UFO|64",
	"1999dachst-ff1.jpg", "61x42|100|24|Felh&#337;k a m&eacute;lyb&#337;l|64",
	"1999dachst-ff2.jpg", "61x42|161|24|...j&ouml;nnek|64",
	"1999dachst-picifelho.jpg", "43x60|222|6|Pici felh&#337;|64"
);

# tátrai képek (1998, 2003)
printf STDERR "Magas-Tatra (1998, 2003)\n";
$yoff += 70;
$p->Annotate(font=>$font, fill=>"#c0c0ff",
	geometry=>"200x16+0+".($yoff+$dytxt+64),
	align=>Left,text=>"Magas-Tátra, 1998. december");
$p->Annotate(font=>$font, fill=>"#c0c0ff",
	geometry=>"200x16+155+".($yoff+$dytxt+92),
	align=>Left,text=>"Svistovy stit DK gerinc, 2003. január");
addpics(5, 20, $yoff, 0,
	"1998tatra-lemente.jpg", "60x43|0|8|Lemente|64",
	"1998tatra-kod.jpg", "42x61|60|0|K&ouml;d|64",
	"1998tatra-alkony.jpg", "61x42|102|15|Alkonyat|64",
	"2003tatra-svistovy.jpg", "92x90|163|0|Nagytarpataki v&ouml;lgy, Svistovy stit d&eacute;l-keleti gerinc, 2003. janu&eacute;r|64"
);

# bakonyi és tündér-sziklai képek (1998)
printf STDERR "Bakony, Kecske-hegy (1998)\n";
$yoff += 90;
$p->Annotate(font=>$font, fill=>"#c0c0ff",
	geometry=>"160x32+0+".($yoff+$dytxt+64),
	align=>Left,text=>"Bakony, 1998. augusztus");
$p->Annotate(font=>$font, fill=>"#c0c0ff",
	geometry=>"160x32+128+".($yoff+$dytxt+64),
	align=>Left,text=>"Kecske-hegy, 1999");
addpics(2, 0, $yoff, 0,
	"1998bakony-hajnal.jpg", "49x53|0|8|K&ouml;d|64",
	"1998bakony-aliensky.jpg", "44x58|49|0|Idegen &eacute;g|64",
	"1998bakony-peter.jpg", "53x48|93|10|&Eacute;n|64",
	"1999kecske-ho1.jpg", "61x42|146|20|Kecske-hegy|64"
);
printf STDERR "Tunder-szikla (1998)\n";
$p->Annotate(font=>$font, fill=>"#c0c0ff",
	geometry=>"290x16+202+".($yoff+$dytxt+14+70),
	align=>Left,text=>"Tündér-szikla, 1998. november");
addpics(3, 202, $yoff+14, 0,
	"1998tndsz-rep.jpg", "59x43|18|15|T&uuml;nd&eacute;r-szikla|64",
#	"1998tndsz-rep-gondonorindo_eldagondosse.jpg", "59x43|77|15|Eldagondo|64",
#	"1998tndsz-top-gondonorindo_eldagondosse.jpg", "40x64|136|5|Fent|64"
);
#$p->Annotate(font=>"-misc-Cirth Erebor-medium-r-normal--9-*-*-*-p-*-iso10646-1", fill=>"#c0c0ff",
#	geometry=>"290x32+240+".($yoff+14+15+43+$dytxt),
#	align=>Left,text=>"za9crbkb");

$yoff += 19;
$p->Annotate(font=>$font, fill=>"#c0c0ff",
	geometry=>"160x32+300+".($yoff+$dytxt+45),
#	geometry=>"160x32+387+".($yoff+$dytxt+42),
	align=>Left,text=>"Pilis, 2003. január");
addpics(4, 300, $yoff, 0,
	"2003pilis-ugras.jpg", "61x42|0|0|Pilis, 2003. janu&aacute;r|64|fot&oacute;: Tam&aacute;s"
);

# schneebergi képek (1997)
printf STDERR "Schneeberg (1997)\n";
$yoff += 71;
$p->Annotate(font=>$font, fill=>"#c0c0ff",
	geometry=>"232x16+0+".($yoff+$dytxt+64),
	align=>Left,text=>"Schneeberg, 1997. december");
addpics(0, 0, $yoff, 0,
	"1997schnee-szikla.jpg", "40x64|0|0|Szikla|64",
	"1997schnee-afeje.jpg", "62x42|40|10|Fej|64",
	"1997schnee-napf3.jpg", "68x38|102|21|Felkelte|64",
	"1997schnee-napf1.jpg", "62x42|170|18|Hajnal|64"
);

# óceán fölötti képek (1998)
printf STDERR "Ocean folott (1998)\n";
$p->Annotate(font=>$font, fill=>"#c0c0ff",
	geometry=>"200x32+270+".($yoff+$dytxt+32),
	align=>Left,text=>"A Nagy Vizek felett,\n            1998. július");
addpics(1, 380, $yoff, 0,
	"1998atlant-felh1+4.jpg", "41x64|0|8|Este &eacute;s reggel|0",
);

print INDEXHTML <<EOF;
var baseurl = "" + location;
if(!baseurl.match("/\$")) {
	baseurl = baseurl.substr(0, baseurl.lastIndexOf("/") + 1);
}
for(var i = 0; i < picLinks.length; ++i) {
	for(var j = 0; j < picLinks[i].length; ++j) {
		picLinks[i][j] = baseurl + picLinks[i][j];
	}
}
//-->
</script>
EOF

printf STDERR "addImageMap()\n";
addImageMap();

# save final image
printf STDERR "Write('index.jpg')\n";
$p->Write('index.jpg');

# 
end_indexhtml();

close(INDEXHTML);

##########################################################
## add pictures in an array to index.jpg and index.html ##
##########################################################

sub addpics {
	my $set = $_[0];
	my $xoff = $_[1];
	my $yoff = $_[2];
	my $annotate = $_[3];
	shift;
	shift;
	shift;
	shift;
	my @pics = @_;
	printf(INDEXHTML "picLinks[%d] = new Array(); ".
			 "picW[%d] = new Array(); ".
			 "picH[%d] = new Array();\n", $set, $set, $set);
	printf(INDEXHTML "picTitle[%d] = new Array(); ".
			 "picNote[%d] = new Array();\n", $set, $set);
	for(my $i = 0; $i < @pics; $i += 2) {
		my $count = $i/2;
		my $f = $pics[$i];
		my @g = split(/\|/, $pics[$i+1]);
		my $x = $xoff + $g[1];
		my $y = $yoff + $g[2];
		my $title = $g[3];
		my $note = ($#g >= 5)? $g[5] : "";
		my $gcomp = $g[0]."+".$x."+".$y;
		my $gtxt = $g[0]."+".$x."+".($y+$dytxt);
		my ($w, $h) = split(/x/, $g[0]);
		my $err = $q->Read($f); die "$err" if "$err";
		my $width = $q->Get("width");
		my $height = $q->Get("height");
		$q->Scale(width=>$w, height=>$h);
		$p->Composite(compose=>Over,geometry=>"$gcomp",image=>$q);
		$p->Annotate(font=>$font, fill=>"#ff0000", geometry=>$gtxt,
			     align=>Left, text=>($count + 1));
		if($annotate) {
			my $gl = $g[0]."+".($xoff+$g[1])."+".($yoff+$g[4]);
			$p->Annotate(font=>$font, fill=>"#c0c0ff",
				     geometry=>$gl,
				     align=>Center, text=>$g[3]);
		}
		undef @$q;
		$picL[$absIndex] = $x;
		$picT[$absIndex] = $y;
		$picR[$absIndex] = $x + $w;
		$picB[$absIndex] = $y + $h;
		$picLinks[$absIndex] = $f;
		$picSetI[$absIndex] = $set;
		++$absIndex;
		printf(INDEXHTML "picLinks[%d][%d] = \"%s\"; ", $set, $count, $f);
		printf(INDEXHTML "picW[%d][%d] = %d; ", $set, $count, $width);
		printf(INDEXHTML "picH[%d][%d] = %d;\n", $set, $count, $height);
		printf(INDEXHTML "picTitle[%d][%d] = \"%s\"; ", $set, $count, $title);
		printf(INDEXHTML "picNote[%d][%d] = \"%s\";\n", $set, $count, $note);
	}
}

sub addImageMap {
	printf(INDEXHTML "<map NAME=\"imgmap\">\n");
	my $k = 0;
	my $prevset = 0;
	for(my $i = 0; $i < @picL; ++$i) {
		my $set = $picSetI[$i];
		if($set != $prevset) {
			$prevset = $set;
			$k = 0;
		}
		printf(INDEXHTML "<area COORDS=\"%d,%d,%d,%d\" HREF=\"%s\"\n".
		"      onClick=\"return show(true, %d, %d)\">\n",
			$picL[$i], $picT[$i], $picR[$i], $picB[$i],
			$picLinks[$i], $set, $k);
		++$k;
	}
	printf(INDEXHTML "</map>\n");
}

##########################################################
################# first part of index.html ###############
##########################################################

sub begin_indexhtml {
	print INDEXHTML <<EOF;
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<title>1997-2003</title>
<script LANGUAGE="JavaScript1.1" SRC="show.js"></script>
<script LANGUAGE="JavaScript1.1">
<!--
function holvagyok() {
	alert('I appear on the following photos: Grossglockner/2, Monte Rosa/2,9, TÃ¡tra/7,9,10, Bakony/3, TÃ¼ndÃ©r-szikla/1-3, "VÃ¡r fÃ¶lÃ¶tt repÃ¼lÃ¶k". My shadow appears on TÃ¡tra/6.');
}
// -->
</script>
</head>
<body BGCOLOR="#202040" TEXT="#c0c0ff" LINK="#8080ff" VLINK="#7820d8" ALINK="#c0c0ff">

<table BORDER="0" CELLSPACING="10" CELLPADDING="0">
<tr><td ALIGN="LEFT" VALIGN="TOP">
EOF
}

##########################################################
##################### end of index.html ##################
##########################################################

sub end_indexhtml() {
	print INDEXHTML <<EOF;
</td><td ALIGN="LEFT" VALIGN="TOP">
<form onSubmit="return false">
<p ALIGN="LEFT">
<input TYPE="BUTTON" VALUE="Where am I?" onClick="holvagyok()">
</p>
</form>
<table BORDER="0" CELLSPACING="5" CELLPADDING="0">
<tr><td NOWRAP ALIGN="LEFT">
    <small>2002. janu&aacute;r</small></td>
    <td NOWRAP ALIGN="LEFT"><a HREF="2002jan/index.html">Kecskehegy
	(trepni), K2 (m&ucirc;fal)</a>
    </td></tr>
<tr><td NOWRAP ALIGN="LEFT">
    <small>2000-2001</small></td>
    <td NOWRAP ALIGN="LEFT"><a HREF="newyork/index.html">New York &amp; Washington</a>
    </td></tr>
</table>
</td></tr>
</table>

</body>
</html>
EOF
}
