all: calc.jar

calc.jar: Calculator.java CalculatorGUI.java
	javac -source 1.2 -target 1.2 Calculator.java CalculatorGUI.java
	echo Main-Class: CalculatorGUI > manifest.tmp
	jar cvmf manifest.tmp calc.jar Calculator*.class CalculatorGUI*.class
	rm manifest.tmp Calculator*.class
