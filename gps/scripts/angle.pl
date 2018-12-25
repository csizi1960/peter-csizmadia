#!/usr/bin/perl -w

use strict;
use Math::Trig;

my $PI = 4*atan2(1,1);
my $deg = $PI/180;

if(@ARGV < 1) {
	print <<EOF;
Usage: perl angle.pl lat0 long0 lat1 long1 lat2 long2

Example:
    perl angle.pl N47 04.622 E12 41.831 3179m N47 04.511 E12 41.562 3699m
EOF
	exit;
}

sub getcoords {
	my $i = 0;
	my $lat = 0;
	my $long = 0;
	my ($arg) = @_;
	for(my $k = 0; $k < 6; ++$k) {
		my $x = @$arg[0];
		if($i == 0) {
			if($x =~ "^N") {
				$lat = substr($x, 1);
			} elsif($x =~ "^S") {
				$lat = -substr($x, 1);
			} else {
				die "bad format, latitude expected".
				       " instead of \"".$x."\"";
			}
			++$i;
			shift @$arg;
		} elsif($i == 1) {
			if($x =~ "^(E|W)") {
				$i += 2;
			} else {
				my $d = $lat > 0? $x/60 : -$x/60;
				$lat += $d;
				++$i;
			}
			shift @$arg;
		} elsif($i == 2) {
			if($x =~ "^(E|W)") {
				++$i;
			} else {
				my $d = $lat > 0? $x/3600 : -$x/3600;
				$lat += $d;
				++$i;
				shift @$arg;
				shift @_;
				next;
			}
		}
		if($i == 3) {
			if($x =~ "^E") {
				$long = substr($x, 1);
			} elsif($x =~ "^W") {
				$long = -substr($x, 1);
			} else {
				die "bad format, longitude expected".
				       " instead of \"".$x."\"";
			}
			++$i;
			shift @$arg;
		} elsif($i == 4) {
			if(!defined($x) || $x =~ "^(N|S)") {
				return ($lat, $long);
			} else {
				my $d = $long > 0? $x/60 : -$x/60;
				$long += $d;
				++$i;
				shift @$arg;
			}
		} elsif($i == 5) {
			if(!defined($x) || $x =~ "^(N|S)") {
				return ($lat, $long);
			} else {
				my $d = $long > 0? $x/3600 : -$x/3600;
				$long += $d;
				++$i;
				shift @$arg;
			}
		}
	}
}

sub lla2dddm {
	my ($lat, $long) = @_;
	my $dlat = $lat >= 0? "N".$lat : "S".(-$lat);
	my $dlong = $long >= 0? "E".$long : "W".(-$long);
	return sprintf("%s %s", $dlat, $dlong);
}

sub lla2xyz {
	my ($lat, $long) = @_;
	my $R = 6375000;
	my $x = $R*cos($lat*$deg)*cos($long*$deg);
	my $y = $R*cos($lat*$deg)*sin($long*$deg);
	my $z = $R*sin($lat*$deg);
	return ($x, $y, $z);
}

sub angle {
	my ($lat0, $long0, $lat1, $long1, $lat2, $long2) = @_;
	my ($x0, $y0, $z0) = lla2xyz($lat0, $long0);
	my ($x1, $y1, $z1) = lla2xyz($lat1, $long1);
	my ($x2, $y2, $z2) = lla2xyz($lat2, $long2);
	my $dx1 = $x1 - $x0;
	my $dy1 = $y1 - $y0;
	my $dz1 = $z1 - $z0;
	my $r1 = sqrt($dx1*$dx1 + $dy1*$dy1 + $dz1*$dz1);
	my $dx2 = $x2 - $x0;
	my $dy2 = $y2 - $y0;
	my $dz2 = $z2 - $z0;
	my $r2 = sqrt($dx2*$dx2 + $dy2*$dy2 + $dz2*$dz2);
	my $cosphi = ($dx1*$dx2 + $dy1*$dy2 + $dz1*$dz2)/($r1*$r2);
	return acos($cosphi);
}

sub hdistance {
	my ($lat1, $long1, $lat2, $long2) = @_;
	my ($x1, $y1, $z1) = lla2xyz($lat1, $long1);
	my ($x2, $y2, $z2) = lla2xyz($lat2, $long2);
	my $dx = $x2 - $x1;
	my $dy = $y2 - $y1;
	my $dz = $z2 - $z1;
	return sqrt($dx*$dx + $dy*$dy + $dz*$dz);
}

my ($lat0, $long0) = getcoords(\@ARGV);
my ($lat1, $long1) = getcoords(\@ARGV);
my ($lat2, $long2) = getcoords(\@ARGV);

printf("0: %s\n", lla2dddm($lat0, $long0));
printf("1: %s\n", lla2dddm($lat1, $long1));
printf("2: %s\n", lla2dddm($lat2, $long2));

printf("d01=%.1fm, d02=%.1fm, phi12=%.1f degrees\n",
	hdistance($lat0, $long0, $lat1, $long1),
	hdistance($lat0, $long0, $lat2, $long2),
	angle($lat0, $long0, $lat1, $long1, $lat2, $long2)/$deg);
