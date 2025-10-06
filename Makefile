lexical_analyzer_win:
	jflex .\src\lexical_analyzer.flex javac .\src\LexicalAnalyzer.java java -cp src LexicalAnalyzer testfiles/Euclid.ycc > testfiles/output.txt

lexical_analyzer_unix:
	jflex ./src/lexical_analyzer.flex;
	javac ./src/LexicalAnalyzer.java ./src/Symbol.java ./src/SymbolTable.java ./src/LexicalUnit.java;
	java -cp src LexicalAnalyzer testfiles/Euclid.ycc > testfiles/output.txt;

clean:
	rm -rf src/*.class src/*.java\~
