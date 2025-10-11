lexical_analyzer_win:
	jflex .\src\lexical_analyzer.flex
	javac -d bin .\src\Main.java .\src\LexicalAnalyzer.java .\src\Symbol.java .\src\SymbolTable.java .\src\LexicalUnit.java .\src\TokenSequence.java
	echo Main-Class: Main > manifest.txt
	jar cfm dist\part1.jar manifest.txt -C bin .
	del /q /f manifest.txt 2> NUL
	
lexical_analyzer_unix:
	jflex ./src/lexical_analyzer.flex
	javac -d bin ./src/Main.java ./src/LexicalAnalyzer.java ./src/Symbol.java ./src/SymbolTable.java ./src/LexicalUnit.java ./src/TokenSequence.java
	echo "Main-Class: Main" > manifest.txt
	jar cfm dist/part1.jar manifest.txt -C bin .
	rm manifest.txt

run:
	java -jar dist/part1.jar test/testProgram.ycc

run_file:
	java -jar dist/part1.jar test/Euclid.ycc > test/output.txt;

doc:
	javadoc -d doc src/*.java

clean_win:
	del /q /f src\*.class src\*.java~ 2> NUL
	rmdir /s /q bin 2> NUL
	del /q /f manifest.txt 2> NUL

clean_unix:
	rm -rf src/*.class src/*.java\~ bin/ manifest.txt