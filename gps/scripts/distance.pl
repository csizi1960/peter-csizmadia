#!/usr/bin/perl -w

use strict;

my $PI = 4*atan2(1,1);
my $deg = $PI/180;

if(@ARGV < 1) {
	print <<EOF;
Usage: perl distance.pl lat1 long1 alt1 lat2 long2 alt2

Example:
    perl distance.pl N47 04.622 E12 41.831 3179m N47 04.511 E12 41.562 3699m
EOF
	exit;
}

sub getcoords {
	my $i = 0;
	my $lat = 0;
	my $long = 0;
	my $alt = 0;
	my ($arg) = @_;
	for(my $k=0; $k<10; ++$k) {
		my $x = shift @$arg;
		if($i == 0) {
			if($x =~ "^N") {
				$lat = substr($x, 1);
				++$i;
			} elsif($x =~ "^S") {
				$lat = -substr($x, 1);
				++$i;
			} else {
				die "bad format, latitude expected".
				       " instead of \"".$x."\"";
			}
		} elsif($i == 1) {
			if($x =~ "^(E|W)") {
				$i += 2;
			} else {
				my $d = $lat > 0? $x/60 : -$x/60;
				$lat += $d;
				++$i;
			}
		} elsif($i == 2) {
			if($x =~ "^(E|W)") {
				++$i;
			} else {
				my $d = $lat > 0? $x/3600 : -$x/3600;
				$lat += $d;
				++$i;
				shift @_;
				next;
			}
		}
		if($i == 3) {
			if($x =~ "^E") {
				$long = substr($x, 1);
				++$i;
			} elsif($x =~ "^W") {
				$long = -substr($x, 1);
				++$i;
			} else {
				die "bad format, longitude expected".
				       " instead of \"".$x."\"";
			}
		} elsif($i == 4) {
			if($x =~ "m\$") {
				$alt = substr($x, 0, length($x) - 1);
				return ($lat, $long, $alt);
			} elsif($x =~ "ft\$") {
				$alt = 0.3048*substr($x, 0, length($x) - 2);
				return ($lat, $long, $alt);
			} else {
				my $d = $long > 0? $x/60 : -$x/60;
				$long += $d;
				++$i;
			}
		} elsif($i == 5) {
			if($x =~ "m\$") {
				$alt = substr($x, 0, length($x) - 1);
				return ($lat, $long, $alt);
			} elsif($x =~ "ft\$") {
				$alt = 0.3048*substr($x, 0, length($x) - 2);
				return ($lat, $long, $alt);
			} else {
				my $d = $long > 0? $x/3600 : -$x/3600;
				$long += $d;
				++$i;
			}
		} elsif($i == 6) {
			if($x =~ "m\$") {
				$alt = substr($x, 0, length($x) - 1);
				return ($lat, $long, $alt);
			} elsif($x =~ "ft\$") {
				$alt = 0.3048*substr($x, 0, length($x) - 2);
				return ($lat, $long, $alt);
			} else {
				die "bad format, altitude expected".
				       " instead of \"".$x."\"";
			}
			++$i;
		}
	}
}

sub lla2dddm {
	my ($lat, $long, $alt) = @_;
	my $dlat = $lat >= 0? "N".$lat : "S".(-$lat);
	my $dlong = $long >= 0? "E".$long : "W".(-$long);
	return sprintf("%s %s %sm", $dlat, $dlong, $alt);
}

sub lla2xyz {
	my ($lat, $long, $alt) = @_;
	my $R = 6375000 + $alt;
	my $x = $R*cos($lat*$deg)*cos($long*$deg);
	my $y = $R*cos($lat*$deg)*sin($long*$deg);
	my $z = $R*sin($lat*$deg);
	return ($x, $y, $z);
}

sub distance {
	my ($lat1, $long1, $alt1, $lat2, $long2, $alt2) = @_;
	my ($x1, $y1, $z1) = lla2xyz($lat1, $long1, $alt1);
	my ($x2, $y2, $z2) = lla2xyz($lat2, $long2, $alt2);
	my $dx = $x2 - $x1;
	my $dy = $y2 - $y1;
	my $dz = $z2 - $z1;
	return sqrt($dx*$dx + $dy*$dy + $dz*$dz);
}

sub hdistance {
	my ($lat1, $long1, $alt1, $lat2, $long2, $alt2) = @_;
	my ($x1, $y1, $z1) = lla2xyz($lat1, $long1, ($alt1 + $alt2)/2);
	my ($x2, $y2, $z2) = lla2xyz($lat2, $long2, ($alt1 + $alt2)/2);
	my $dx = $x2 - $x1;
	my $dy = $y2 - $y1;
	my $dz = $z2 - $z1;
	return sqrt($dx*$dx + $dy*$dy + $dz*$dz);
}

sub bevel {
	my ($lat1, $long1, $alt1, $lat2, $long2, $alt2) = @_;
	my $hd = hdistance($lat1, $long1, $alt1, $lat2, $long2, $alt2);
	my $vd = $alt2 - $alt1;
	return atan2($vd, $hd);
}

my ($lat1, $long1, $alt1) = getcoords(\@ARGV);
my ($lat2, $long2, $alt2) = getcoords(\@ARGV);

printf("1: %s\n", lla2dddm($lat1, $long1, $alt1));
printf("2: %s\n", lla2dddm($lat2, $long2, $alt2));

printf("d12=%.1fm, dx=%.1fm, dz=%gm, %.1f degrees\n",
	distance($lat1, $long1, $alt1, $lat2, $long2, $alt2),
	hdistance($lat1, $long1, $alt1, $lat2, $long2, $alt2), $alt2-$alt1,
	bevel($lat1, $long1, $alt1, $lat2, $long2, $alt2)/$deg);
