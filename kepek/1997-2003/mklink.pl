#!/usr/bin/perl -w

use Image::Magick;

# make initial empty image
my $p = new Image::Magick;
my $font = "-adobe-helvetica-medium-r-normal--10-*-*-*-p-*-iso8859-2";
$p->Set(size=>"544x66");
$p->Read("xc:#ffffff");

# create image object for thumbnails
my $q = new Image::Magick;

my $yoff = 0;
my $dytxt = 10;

addpics(0, 0, $yoff, 0,
	"1997schnee-szikla.jpg", "40x64|0|0",
	"1998tndsz-rep.jpg", "59x43|0|0",
	"2003pilis-ugras.jpg", "61x42|0|0",
	"1998tatra-lemente.jpg", "60x43|0|0",
	"2000tatra-vilagvege2.jpg", "60x43|0|0",
	"1999dachst-ff1.jpg", "61x42|0|0",
	"2001monterosa-grenzgipfel.jpg", "62x42|0|0",
	"2002li-gerinc.jpg", "61x43|0|0",
	"2002glockner-studlgrat.jpg", "42x62|0|0|Stüdlgrat (III, III+)|64",
	"2002glockner-csucs.jpg", "38x62|0|0|3798m|64"
);

$p->Annotate(font=>$font, fill=>"#000000", geometry=>"+42+50", align=>Left,
text=>"Schneeberg, Tündér-szikla, levitáció, Magas-Tátra, Dachstein, Monte Rosa, Liechtenstein,\n".
"Großglockner/Stüdlgrat, Kecske-hegy, mûfal (K2), Bakony, New York+Washington");

# save final image
$p->Write('../1997-2003.jpg');


##########################################
## add pictures in an array to link.jpg ##
##########################################

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
	for(my $i = 0; $i < @pics; $i += 2) {
		my $f = $pics[$i];
		my @g = split(/\|/, $pics[$i+1]);
		my $x = $xoff + $g[1];
		my $y = $yoff + $g[2];
		my $gcomp = $g[0]."+".$x."+".$y;
		my $gtxt = $g[0]."+".$x."+".($y+$dytxt);
		my ($w, $h) = split(/x/, $g[0]);
		my $err = $q->Read($f); die "$err" if "$err";
		my $width = $q->Get("width");
		my $height = $q->Get("height");
		$q->Scale(width=>$w, height=>$h);
		$p->Composite(compose=>Over,geometry=>"$gcomp",image=>$q);
		undef @$q;
		$xoff += $w;
	}
	printf("width=$xoff\n");
}
