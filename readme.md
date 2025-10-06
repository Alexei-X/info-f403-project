Pour lancer :

jflex .\src\lexical_analyzer.flex
javac .\src\LexicalAnalyzer.java
java -cp src LexicalAnalyzer testfiles/Euclid.ycc > testfiles/output.txt
