lexical_analyzer:
	jflex src/lexical_analyzer.flex
	javac src/LexicalAnalyzer.java

clean:
	rm -rf src/*.class src/*.java\~
