#!/usr/bin/perl

use CGI qw(param);

$IMGOFF = 64;
$shmid = param('shmid');
$len = param('len');
$time = param('time');
$offset = length($head);
shmread($shmid, $head, 0, $IMGOFF);
($len1, $time1) = unpack("II", $head);
if($len1 == $len && $time1 == $time) {
	my $g;
	shmread($shmid, $g, 64, $len-64);
	shmctl($shmid, IPC_RMID, 0);
	printf("Content-type: image/gif\n\n");
	print $g;
}
