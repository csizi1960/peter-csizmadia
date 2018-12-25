all: fredkinbbm.jar

#EXTRAFILES = Makefile

fredkinbbm.jar: FredkinBBM.java
	javac -source 1.2 -target 1.2 FredkinBBM.java
	echo Main-Class: FredkinBBM > manifest.tmp
	jar cvmf manifest.tmp fredkinbbm.jar FredkinBBM*.class
	rm manifest.tmp FredkinBBM*.class

#tar:
#	tar cjvf extra.tar.bz2 $(EXTRAFILES)
#	rm $(EXTRAFILES)
