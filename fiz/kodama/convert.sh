#!/bin/sh -e

f="massless-c7.html massless-c7.148.html shell.html shell_C_B_1.html centered.html"
jdkhome="/usr/java/jdk1.5.0_09"
htmlconverter="$jdkhome/bin/java -jar $jdkhome/lib/htmlconverter.jar"

rm -rf hc
mkdir hc
cp -p $f hc/
cd hc
$htmlconverter -latest *.html
for f in *.html; do
	cp -p $f ../hc_$f
done
cd ..
rm -rf hc_BAK hc
