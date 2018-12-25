#!/usr/bin/perl -w
use Image::Magick;

my $p = new Image::Magick;

$p->Set(size=>"640x731");
$p->Read("xc:#202040");
my $q1 = new Image::Magick;
$q1->Read("2001ny-brooklynbridge.png");
$p->Composite(composite=>Over,geometry=>"640x389+0+342",image=>$q1);
my $q2 = new Image::Magick;
$q2->Read("1998ny-brooklynbridgewtc.png");
$p->Composite(composite=>Over,geometry=>"640x344+0+0",image=>$q2);
$p->Annotate(fill=>"#000000", geometry=>"200x16+25+20", align=>Left,
		text=>"July 1998");
$p->Annotate(fill=>"#ffffff", geometry=>"200x16+25+364", align=>Left,
		text=>"October 2001");
$p->Write('2001ny+1998-BrooklynBridge.jpg');
