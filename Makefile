part1_win:
	if not exist bin mkdir bin
	if not exist dist mkdir dist
	jflex .\src\LexicalAnalyzer\lexical_analyzer.flex
	javac -d bin .\src\Main.java .\src\LexicalAnalyzer\LexicalAnalyzer.java .\src\LexicalAnalyzer\Symbol.java .\src\LexicalAnalyzer\SymbolTable.java .\src\LexicalAnalyzer\LexicalUnit.java .\src\LexicalAnalyzer\TokenSequence.java .\src\LexicalAnalyzer\NonTermUnit.java .\src\Parser\ParseException.java .\src\Parser\Parser.java .\src\Parser\ParseTree.java .\src\Parser\Pair.java .\src\LlvmGenerator\LLVMGenerator.java
	echo Main-Class: Main > manifest.txt
	jar cfm dist\part1.jar manifest.txt -C bin .
	del /q /f manifest.txt 2> NUL

part1_unix:
	jflex ./src/LexicalAnalyzer/lexical_analyzer.flex
	javac -d bin ./src/Main.java ./src/LexicalAnalyzer/LexicalAnalyzer.java ./src/LexicalAnalyzer/Symbol.java ./src/LexicalAnalyzer/SymbolTable.java ./src/LexicalAnalyzer/LexicalUnit.java ./src/LexicalAnalyzer/TokenSequence.java ./src/Parser/ParseException.java ./src/Parser/Parser.java ./src/Parser/ParseTree.java ./src/Parser/Pair.java ./src/LexicalAnalyzer/NonTermUnit.java
	echo "Main-Class: Main" > manifest.txt
	jar cfm dist/part1.jar manifest.txt -C bin .
	rm manifest.txt
	
part2_win:
	if not exist bin mkdir bin
	if not exist dist mkdir dist
	jflex .\src\LexicalAnalyzer\lexical_analyzer.flex
	javac -d bin .\src\Main.java .\src\LexicalAnalyzer\LexicalAnalyzer.java .\src\LexicalAnalyzer\Symbol.java .\src\LexicalAnalyzer\SymbolTable.java .\src\LexicalAnalyzer\LexicalUnit.java .\src\LexicalAnalyzer\TokenSequence.java .\src\Parser\ParseException.java .\src\Parser\Parser.java .\src\Parser\ParseTree.java .\src\Parser\Pair.java .\src\LexicalAnalyzer\NonTermUnit.java .\src\LlvmGenerator\LLVMGenerator.java
	echo Main-Class: Main > manifest.txt
	jar cfm dist\part2.jar manifest.txt -C bin .
	del /q /f manifest.txt 2> NUL

part3_unix:
	jflex ./src/LexicalAnalyzer/lexical_analyzer.flex
	javac -d bin ./src/Main.java ./src/LexicalAnalyzer/LexicalAnalyzer.java ./src/LexicalAnalyzer/Symbol.java ./src/LexicalAnalyzer/SymbolTable.java ./src/LexicalAnalyzer/LexicalUnit.java ./src/LexicalAnalyzer/TokenSequence.java ./src/Parser/ParseException.java ./src/Parser/Parser.java ./src/Parser/ParseTree.java ./src/Parser/Pair.java ./src/LexicalAnalyzer/NonTermUnit.java ./src/LlvmGenerator/LLVMGenerator.java
	echo "Main-Class: Main" > manifest.txt
	jar cfm dist/part3.jar manifest.txt -C bin .
	rm manifest.txt



##Run
run_part1:
	java -jar dist/part1.jar test/input/Euclid.ycc

run_part2:
	java -jar dist/part2.jar test/input/Euclid.ycc


##Documentation
doc:
	javadoc -d doc -sourcepath src src/LexicalAnalyzer/*.java src/Parser/*.java src/Main.java

##Cleaning
clean_win:
	if exist bin rmdir /s /q bin
	if exist dist\part2.jar del /q /f dist\part2.jar
	if exist dist\part1.jar del /q /f dist\part1.jar
	if exist manifest.txt del /q /f manifest.txt
	if exist src\*.class del /q /f src\*.class
	if exist src\*.java~ del /q /f src\*.java~
	if exist src\Parser\*.class del /q /f src\Parser\*.class
	if exist src\LexicalAnalyzer\*.class del /q /f src\LexicalAnalyzer\*.class
	if exist src\LexicalAnalyzer\LexicalAnalyzer.java del /q /f src\LexicalAnalyzer\LexicalAnalyzer.java
	if exist test\output\*.output del /q /f test\output\*.output
	if exist test\output\ParsingOutput.txt del /q /f test\output\ParsingOutput.txt
	if exist test\output\LexicalAnalyzerOutput.txt del /q /f test\output\LexicalAnalyzerOutput.txt
	if exist test\output\*.ll del /q /f test\output\*.ll
	if exist test\bin\*.exe del /q /f test\bin\*.exe

clean_unix:
	rm -rf src/*.class src/*.java~ src/Parser/*.class src/LexicalAnalyzer/*.class bin/ manifest.txt
	rm -f dist/part3.jar dist/part2.jar dist/part1.jar src/LexicalAnalyzer/LexicalAnalyzer.java
	rm -f test/output/*.output test/output/ParsingOutput.txt test/output/LexicalAnalyzerOutput.txt test/output/*.ll test/output/*.bc
	rm -f test/bin/*

#Run LLVM
run_llvm_win:
	java -jar dist\part2.jar test\input\Euclid.ycc
	clang test\output\Euclid.ll -o test\bin\Euclid.exe
	.\test\bin\Euclid.exe

run_llvm_unix:
	java -jar dist/part2.jar test/input/Euclid.ycc
	clang test/output/Euclid.ll -o test/bin/Euclid
	./test/bin/Euclid
