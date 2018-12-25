#!/bin/sh

infile="$1"

if [ -z "$infile" ]; then
	echo usage: gpsman2mapsource.sh file.gpsman
fi
outfile=`echo $infile | sed 's/\.gpsman$/.gdb/'`

if [ "$infile" = "$outfile" ]; then
	outfile="$infile.gdb"
fi
grep -v '^[%!]\|^$' $infile >$infile.tmp
gpsbabel -i gpsman,snlen=10 -f $infile.tmp -o gdb,ver=1 -F $outfile \
&& touch -r $infile $outfile
r=$?
rm -f $infile.tmp
exit $r
