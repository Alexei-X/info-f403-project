
part1_win:
	jflex .\src\LexicalAnalyzer\lexical_analyzer.flex
	javac -d bin .\src\Main.java .\src\LexicalAnalyzer\LexicalAnalyzer.java .\src\LexicalAnalyzer\Symbol.java .\src\LexicalAnalyzer\SymbolTable.java .\src\LexicalAnalyzer\LexicalUnit.java .\src\LexicalAnalyzer\TokenSequence.java
	echo Main-Class: Main > manifest.txt
	jar cfm dist\part1.jar manifest.txt -C bin .
	del /q /f manifest.txt 2> NUL

part1_unix:
	jflex ./src/LexicalAnalyzer/lexical_analyzer.flex
	javac -d bin ./src/Main.java ./src/LexicalAnalyzer/LexicalAnalyzer.java ./src/LexicalAnalyzer/Symbol.java ./src/LexicalAnalyzer/SymbolTable.java ./src/LexicalAnalyzer/LexicalUnit.java ./src/LexicalAnalyzer/TokenSequence.java
	echo "Main-Class: Main" > manifest.txt
	jar cfm dist/part1.jar manifest.txt -C bin .
	rm manifest.txt
	
part2_win:
	jflex .\src\LexicalAnalyzer\lexical_analyzer.flex
	javac -d bin .\src\Main.java .\src\LexicalAnalyzer\LexicalAnalyzer.java .\src\LexicalAnalyzer\Symbol.java .\src\LexicalAnalyzer\SymbolTable.java .\src\LexicalAnalyzer\LexicalUnit.java .\src\LexicalAnalyzer\TokenSequence.java .\src\Parser\ParseException .\src\Parser\Parser .\src\Parser\ParseTree .\src\Parser\Pair .\src\LexicalAnalyzer\NonTermUnit
	echo Main-Class: Main > manifest.txt
	jar cfm dist\part2.jar manifest.txt -C bin .
	del /q /f manifest.txt 2> NUL

part2_unix:
	jflex ./src/LexicalAnalyzer/lexical_analyzer.flex
	javac -d bin ./src/Main.java ./src/LexicalAnalyzer/LexicalAnalyzer.java ./src/LexicalAnalyzer/Symbol.java ./src/LexicalAnalyzer/SymbolTable.java ./src/LexicalAnalyzer/LexicalUnit.java ./src/LexicalAnalyzer/TokenSequence.java ./src/Parser/ParseException.java ./src/Parser/Parser.java ./src/Parser/ParseTree.java ./src/Parser/Pair.java ./src/LexicalAnalyzer/NonTermUnit.java
	echo "Main-Class: Main" > manifest.txt
	jar cfm dist/part2.jar manifest.txt -C bin .
	rm manifest.txt


##Run
run_part1:
	java -jar dist/part1.jar test/Euclid.ycc #> test/LexicalAnalyzerOutput.txt

run_part2:
	java -jar dist/part2.jar test/Euclid.ycc


##Documentation
doc:
	javadoc -d doc src/*.java


##Cleaning
clean_win:
	if exist bin rmdir /s /q bin
	if exist dist\part2.jar del /q /f dist\parser.jar
	if exist dist\part1.jar del /q /f dist\part1.jar
	if exist manifest.txt del /q /f manifest.txt
	if exist src\*.class del /q /f src\*.class
	if exist src\*.java~ del /q /f src\*.java~
	if exist src\Parser\*.class del /q /f src\Parser\*.class
	if exist src\LexicalAnalyzer\*.class del /q /f src\LexicalAnalyzer\*.class
	if exist src\LexicalAnalyzer\LexicalAnalyzer.java del /q /f src\LexicalAnalyzer\LexicalAnalyzer.java
	if exist test\*.output del /q /f test\*.output
	if exist test\ParsingOutput.txt del /q /f test\ParsingOutput.txt
	if exist test\LexicalAnalyzerOutput.txt del /q /f test\LexicalAnalyzerOutput.txt

clean_unix:
	rm -rf src/*.class src/*.java~ src/Parser/*.class src/LexicalAnalyzer/*.class bin/ manifest.txt
	rm -f dist/part2.jar dist/part1.jar src/LexicalAnalyzer/LexicalAnalyzer.java
	rm -f test/*.output test/ParsingOutput.txt test/LexicalAnalyzerOutput.txt
