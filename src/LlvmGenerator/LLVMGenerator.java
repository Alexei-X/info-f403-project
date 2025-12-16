package llvmGenerator;

import Parser.ParseTree;
import LexicalAnalyzer.LexicalUnit;
import LexicalAnalyzer.Symbol;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LLVM IR code generator for yaLcc language
 * Traverses the parse tree and generates LLVM intermediate representation code
 * 
 * @author Mohamed Tajani and Alex Bataille
 */

public class LLVMGenerator {
    /** Symbol table mapping variable names to LLVM register numbers */
    private Map<String, String> symbolTable;
    
    /** Counter for generating unique LLVM temporary registers */
    //On tient le compte pour avoir un registre unique à chaque nouvelle declaration
    private int registerCounter;
    
    /** Counter for generating unique labels */
    // idem mais pour les labels
    private int labelCounter;
    
    /** StringBuilder for accumulating the LLVM code */
    private StringBuilder llvmCode;
    
    /** StringBuilder for accumulating declarations (global variables, function declarations) */
    private StringBuilder declarations;
    
    /**
     * Creates a new LLVM code generator
     */
    public LLVMGenerator() {
        this.symbolTable = new HashMap<>();
        this.registerCounter = 1;
        this.labelCounter = 0;
        this.llvmCode = new StringBuilder();
        this.declarations = new StringBuilder();
        initializeDeclarations();
    }
    
    /**
     * Initializes the necessary LLVM declarations for I/O operations
     */
    private void initializeDeclarations() {
        // Declare external functions for I/O
        declarations.append("; Declare external functions for I/O\n");
        declarations.append("declare i32 @printf(i8*, ...)\n");
        declarations.append("declare i32 @scanf(i8*, ...)\n");
        declarations.append("\n");
        
        // Format strings for printf and scanf
        declarations.append("; Format strings\n");
        declarations.append("@.str_int = private unnamed_addr constant [4 x i8] c\"%d\\0A\\00\", align 1\n");
        declarations.append("@.str_read = private unnamed_addr constant [3 x i8] c\"%d\\00\", align 1\n");
        declarations.append("\n");
    }
    
    /**
     * Generates a new unique temporary register name
     * 
     * @return A new register name (e.g., "%1", "%2", etc.)
     */
    private String newRegister() {
        return "%" + (registerCounter++);
    }
    
    /**
     * Generates a new unique label name
     * 
     * @return A new label name (e.g., "label1", "label2", etc.)
     */
    private String newLabel() {
        return "label" + (labelCounter++);
    }
    
    /**
     * Gets or creates an LLVM register for a variable
     * 
     * @param varName The variable name
     * @return The LLVM register associated with this variable
     */
    private String getOrCreateVariable(String varName) {
        if (!symbolTable.containsKey(varName)) {
            String reg = newRegister();
            symbolTable.put(varName, reg);
            // Allocate space for the variable
            llvmCode.append("  ").append(reg).append(" = alloca i32, align 4\n");
        }
        return symbolTable.get(varName);
    }
    
    /**
     * Generates LLVM IR code from a parse tree
     * 
     * @param tree The parse tree to generate code from
     * @return The complete LLVM IR code as a string
     */
    public String generate(ParseTree tree) {
        StringBuilder result = new StringBuilder();
        result.append(declarations);
        
        // Start main function
        result.append("define i32 @main() {\n");
        
        // Generate code for the program
        generateProgram(tree);
        
        // Add return statement
        llvmCode.append("  ret i32 0\n");
        result.append(llvmCode);
        result.append("}\n");
        
        return result.toString();
    }
    
    /**
     * Generates code for the Program non-terminal
     * Program -> Prog [ProgName] Is Code End
     * 
     * @param tree The parse tree node for Program
     */
    private void generateProgram(ParseTree tree) {
        List<ParseTree> children = tree.getChildren();
        if (children == null || children.size() < 5) {
            throw new RuntimeException("Invalid Program structure");
        }
        
        // children[0] = Prog keyword
        // children[1] = ProgName
        // children[2] = Is keyword
        // children[3] = Code
        // children[4] = End keyword
        
        generateCode(children.get(3));
    }
    
    /**
     * Generates code for the Code non-terminal
     * Code -> Instruction ; Code | epsilon
     * 
     * @param tree The parse tree node for Code
     */
    private void generateCode(ParseTree tree) {
        List<ParseTree> children = tree.getChildren();
        if (children == null || children.isEmpty()) {
            // Epsilon production
            return;
        }
        
        // children[0] = Instruction
        // children[1] = semicolon
        // children[2] = Code (recursive)
        
        generateInstruction(children.get(0));
        if (children.size() > 2) {
            generateCode(children.get(2));
        }
    }
    
    /**
     * Generates code for the Instruction non-terminal
     * 
     * @param tree The parse tree node for Instruction
     */
    private void generateInstruction(ParseTree tree) {
        List<ParseTree> children = tree.getChildren();
        if (children == null || children.isEmpty()) {
            return;
        }
        
        ParseTree instruction = children.get(0);
        Symbol symbol = instruction.getLabel();
        
        String symbolStr = symbol.getValue();
        
        if (symbolStr.contains("Assign")) {
            generateAssign(instruction);
        } else if (symbolStr.contains("If")) {
            generateIf(instruction);
        } else if (symbolStr.contains("While")) {
            generateWhile(instruction);
        } else if (symbolStr.contains("Output")) {
            generateOutput(instruction);
        } else if (symbolStr.contains("Input")) {
            generateInput(instruction);
        }
    }
    
    /**
     * Generates code for an assignment instruction
     * Assign -> [VarName] = ExprArith
     * 
     * @param tree The parse tree node for Assign
     */
    private void generateAssign(ParseTree tree) {
        List<ParseTree> children = tree.getChildren();
        if (children == null || children.size() < 3) {
            throw new RuntimeException("Invalid Assign structure");
        }
        
        // children[0] = VarName
        // children[1] = =
        // children[2] = ExprArith
        
        String varName = children.get(0).getLabel().getValue();
        String varReg = getOrCreateVariable(varName);
        
        String exprReg = generateExprArith(children.get(2));
        
        // Store the result in the variable
        llvmCode.append("  store i32 ").append(exprReg).append(", i32* ").append(varReg).append(", align 4\n");
    }
    
    /**
     * Generates code for an arithmetic expression
     * ExprArith -> Prod ExprArith'
     * 
     * @param tree The parse tree node for ExprArith
     * @return The LLVM register containing the result
     */
    private String generateExprArith(ParseTree tree) {
        List<ParseTree> children = tree.getChildren();
        if (children == null || children.size() < 2) {
            throw new RuntimeException("Invalid ExprArith structure");
        }
        
        // children[0] = Prod
        // children[1] = ExprArith'
        
        String prodReg = generateProd(children.get(0));
        return generateExprArithPrime(children.get(1), prodReg);
    }
    
    /**
     * Generates code for ExprArith' (handles + and - operations)
     * ExprArith' -> + Prod ExprArith' | - Prod ExprArith' | epsilon
     * 
     * @param tree The parse tree node for ExprArith'
     * @param leftReg The register containing the left operand
     * @return The LLVM register containing the result
     */
    private String generateExprArithPrime(ParseTree tree, String leftReg) {
        List<ParseTree> children = tree.getChildren();
        if (children == null || children.isEmpty()) {
            // Epsilon production
            return leftReg;
        }
        
        // children[0] = operator (+ or -)
        // children[1] = Prod
        // children[2] = ExprArith' (recursive)
        
        String operator = children.get(0).getLabel().getType().toString();
        String rightReg = generateProd(children.get(1));
        
        String resultReg = newRegister();
        if (operator.equals("PLUS")) {
            llvmCode.append("  ").append(resultReg).append(" = add nsw i32 ").append(leftReg).append(", ").append(rightReg).append("\n");
        } else if (operator.equals("MINUS")) {
            llvmCode.append("  ").append(resultReg).append(" = sub nsw i32 ").append(leftReg).append(", ").append(rightReg).append("\n");
        }
        
        // Continue with the rest of ExprArith'
        if (children.size() > 2) {
            return generateExprArithPrime(children.get(2), resultReg);
        }
        return resultReg;
    }
    
    /**
     * Generates code for a product expression
     * Prod -> Atom Prod'
     * 
     * @param tree The parse tree node for Prod
     * @return The LLVM register containing the result
     */
    private String generateProd(ParseTree tree) {
        List<ParseTree> children = tree.getChildren();
        if (children == null || children.size() < 2) {
            throw new RuntimeException("Invalid Prod structure");
        }
        
        // children[0] = Atom
        // children[1] = Prod'
        
        String atomReg = generateAtom(children.get(0));
        return generateProdPrime(children.get(1), atomReg);
    }
    
    /**
     * Generates code for Prod' (handles * and / operations)
     * Prod' -> * Atom Prod' | / Atom Prod' | epsilon
     * 
     * @param tree The parse tree node for Prod'
     * @param leftReg The register containing the left operand
     * @return The LLVM register containing the result
     */
    private String generateProdPrime(ParseTree tree, String leftReg) {
        List<ParseTree> children = tree.getChildren();
        if (children == null || children.isEmpty()) {
            // Epsilon production
            return leftReg;
        }
        
        // children[0] = operator (* or /)
        // children[1] = Atom
        // children may have [2] = Prod' (recursive) if continuing
        
        String operator = children.get(0).getLabel().getType().toString();
        String rightReg = generateAtom(children.get(1));
        
        String resultReg = newRegister();
        if (operator.equals("TIMES")) {
            llvmCode.append("  ").append(resultReg).append(" = mul nsw i32 ").append(leftReg).append(", ").append(rightReg).append("\n");
        } else if (operator.equals("DIVIDE")) {
            llvmCode.append("  ").append(resultReg).append(" = sdiv i32 ").append(leftReg).append(", ").append(rightReg).append("\n");
        }
        
        // Note: Prod' after * or / becomes epsilon in our grammar, so we return here
        return resultReg;
    }
    
    /**
     * Generates code for an atomic expression
     * Atom -> [VarName] | [Number] | - Atom | ( ExprArith )
     * 
     * @param tree The parse tree node for Atom
     * @return The LLVM register containing the result
     */
    private String generateAtom(ParseTree tree) {
        List<ParseTree> children = tree.getChildren();
        if (children == null || children.isEmpty()) {
            throw new RuntimeException("Invalid Atom structure");
        }
        
        ParseTree firstChild = children.get(0);
        LexicalUnit type = firstChild.getLabel().getType();
        
        if (type == LexicalUnit.VARNAME) {
            // Load variable value
            String varName = firstChild.getLabel().getValue();
            String varReg = getOrCreateVariable(varName);
            String resultReg = newRegister();
            llvmCode.append("  ").append(resultReg).append(" = load i32, i32* ").append(varReg).append(", align 4\n");
            return resultReg;
        } else if (type == LexicalUnit.NUMBER) {
            // Return the constant value
            return firstChild.getLabel().getValue();
        } else if (type == LexicalUnit.MINUS) {
            // Unary minus: 0 - Atom
            String atomReg = generateAtom(children.get(1));
            String resultReg = newRegister();
            llvmCode.append("  ").append(resultReg).append(" = sub nsw i32 0, ").append(atomReg).append("\n");
            return resultReg;
        } else if (type == LexicalUnit.LPAREN) {
            // Parenthesized expression
            return generateExprArith(children.get(1));
        }
        
        throw new RuntimeException("Unknown Atom type: " + type);
    }
    
    /**
     * Generates code for an If statement
     * If -> If [ Cond ] Then Code C
     * C -> End | Else Code End
     * 
     * @param tree The parse tree node for If
     */
    private void generateIf(ParseTree tree) {
        List<ParseTree> children = tree.getChildren();
        if (children == null || children.size() < 7) {
            throw new RuntimeException("Invalid If structure");
        }
        
        // children[0] = If keyword
        // children[1] = [
        // children[2] = Cond
        // children[3] = ]
        // children[4] = Then keyword
        // children[5] = Code (then branch)
        // children[6] = C (End or Else Code End)
        
        String thenLabel = newLabel();
        String elseLabel = newLabel();
        String endLabel = newLabel();
        
        // Generate condition code
        String condReg = generateCond(children.get(2), thenLabel, elseLabel);
        
        // Then branch
        llvmCode.append(thenLabel).append(":\n");
        generateCode(children.get(5));
        llvmCode.append("  br label %").append(endLabel).append("\n");
        
        // Else branch (if present)
        llvmCode.append(elseLabel).append(":\n");
        ParseTree cNode = children.get(6);
        List<ParseTree> cChildren = cNode.getChildren();
        if (cChildren != null && !cChildren.isEmpty() && cChildren.get(0).getLabel().getType() == LexicalUnit.ELSE) {
            // Has else branch: children[1] = Code
            generateCode(cChildren.get(1));
        }
        llvmCode.append("  br label %").append(endLabel).append("\n");
        
        // End label
        llvmCode.append(endLabel).append(":\n");
    }
    
    /**
     * Generates code for a While loop
     * While -> While [ Cond ] Do Code End
     * 
     * @param tree The parse tree node for While
     */
    private void generateWhile(ParseTree tree) {
        List<ParseTree> children = tree.getChildren();
        if (children == null || children.size() < 7) {
            throw new RuntimeException("Invalid While structure");
        }
        
        // children[0] = While keyword
        // children[1] = [
        // children[2] = Cond
        // children[3] = ]
        // children[4] = Do keyword
        // children[5] = Code (loop body)
        // children[6] = End keyword
        
        String condLabel = newLabel();
        String bodyLabel = newLabel();
        String endLabel = newLabel();
        
        // Jump to condition
        llvmCode.append("  br label %").append(condLabel).append("\n");
        
        // Condition label
        llvmCode.append(condLabel).append(":\n");
        String condReg = generateCond(children.get(2), bodyLabel, endLabel);
        
        // Body label
        llvmCode.append(bodyLabel).append(":\n");
        generateCode(children.get(5));
        llvmCode.append("  br label %").append(condLabel).append("\n");
        
        // End label
        llvmCode.append(endLabel).append(":\n");
    }
    
    /**
     * Generates code for a condition and branching
     * Cond -> CondA
     * 
     * @param tree The parse tree node for Cond
     * @param trueLabel The label to jump to if condition is true
     * @param falseLabel The label to jump to if condition is false
     * @return The register containing the boolean result
     */
    private String generateCond(ParseTree tree, String trueLabel, String falseLabel) {
        List<ParseTree> children = tree.getChildren();
        if (children == null || children.isEmpty()) {
            throw new RuntimeException("Invalid Cond structure");
        }
        
        // Cond -> CondA
        String condReg = generateCondA(children.get(0));
        
        // Branch based on condition
        llvmCode.append("  br i1 ").append(condReg).append(", label %").append(trueLabel).append(", label %").append(falseLabel).append("\n");
        
        return condReg;
    }
    
    /**
     * Generates code for CondA
     * CondA -> | Cond | ExprArith D
     * 
     * @param tree The parse tree node for CondA
     * @return The register containing the boolean result
     */
    private String generateCondA(ParseTree tree) {
        List<ParseTree> children = tree.getChildren();
        if (children == null || children.isEmpty()) {
            throw new RuntimeException("Invalid CondA structure");
        }
        
        ParseTree firstChild = children.get(0);
        
        // Check if it's a negation (| Cond |)
        if (firstChild.getLabel().getType() == LexicalUnit.PIPE) {
            // This is negation - but wait, the grammar shows this differently
            // Let me check: CondA -> | Cond |
            // Actually in our grammar: CondA -> Cond PIPE | ExprArith D
            // Let me re-examine...
        }
        
        // Check the symbol to determine which production
        String symbolStr = firstChild.getLabel().getValue();
        if (symbolStr.contains("Cond")) {
            // CondA -> Cond |
            String innerCondReg = generateCondHelper(children.get(0));
            // Negate the condition
            String resultReg = newRegister();
            llvmCode.append("  ").append(resultReg).append(" = xor i1 ").append(innerCondReg).append(", true\n");
            return resultReg;
        } else {
            // CondA -> ExprArith D
            String leftReg = generateExprArith(children.get(0));
            return generateD(children.get(1), leftReg);
        }
    }
    
    /**
     * Helper to generate condition code (handles nested conditions and comparisons)
     * 
     * @param tree The parse tree node
     * @return The register containing the boolean result
     */
    private String generateCondHelper(ParseTree tree) {
        List<ParseTree> children = tree.getChildren();
        if (children == null || children.isEmpty()) {
            throw new RuntimeException("Invalid condition structure");
        }
        
        return generateCondA(children.get(0));
    }
    
    /**
     * Generates code for comparison operators
     * D -> == ExprArith | <= ExprArith | < ExprArith
     * 
     * @param tree The parse tree node for D
     * @param leftReg The register containing the left operand
     * @return The register containing the boolean result
     */
    private String generateD(ParseTree tree, String leftReg) {
        List<ParseTree> children = tree.getChildren();
        if (children == null || children.size() < 2) {
            throw new RuntimeException("Invalid D structure");
        }
        
        // children[0] = comparison operator
        // children[1] = ExprArith
        
        LexicalUnit operator = children.get(0).getLabel().getType();
        String rightReg = generateExprArith(children.get(1));
        
        String resultReg = newRegister();
        String comparison = "";
        
        switch (operator) {
            case EQUAL:
                comparison = "eq";
                break;
            case SMALEQ:
                comparison = "sle";
                break;
            case SMALLER:
                comparison = "slt";
                break;
            default:
                throw new RuntimeException("Unknown comparison operator: " + operator);
        }
        
        llvmCode.append("  ").append(resultReg).append(" = icmp ").append(comparison).append(" i32 ").append(leftReg).append(", ").append(rightReg).append("\n");
        
        return resultReg;
    }
    
    /**
     * Generates code for Print statement
     * Output -> Print ( [VarName] )
     * 
     * @param tree The parse tree node for Output
     */
    private void generateOutput(ParseTree tree) {
        List<ParseTree> children = tree.getChildren();
        if (children == null || children.size() < 4) {
            throw new RuntimeException("Invalid Output structure");
        }
        
        // children[0] = Print keyword
        // children[1] = (
        // children[2] = VarName
        // children[3] = )
        
        String varName = children.get(2).getLabel().getValue();
        String varReg = getOrCreateVariable(varName);
        
        // Load the variable value
        String valueReg = newRegister();
        llvmCode.append("  ").append(valueReg).append(" = load i32, i32* ").append(varReg).append(", align 4\n");
        
        // Call printf
        String formatReg = newRegister();
        String secReg = newRegister();
        llvmCode.append("  ").append(formatReg).append(" = getelementptr inbounds [4 x i8], [4 x i8]* @.str_int, i32 0, i32 0\n");
        llvmCode.append("  ").append(secReg).append(" = call i32 (i8*, ...) @printf(i8* ").append(formatReg).append(", i32 ").append(valueReg).append(")\n");
    }
    
    /**
     * Generates code for Input statement
     * Input -> Input ( [VarName] )
     * 
     * @param tree The parse tree node for Input
     */
    private void generateInput(ParseTree tree) {
        List<ParseTree> children = tree.getChildren();
        if (children == null || children.size() < 4) {
            throw new RuntimeException("Invalid Input structure");
        }
        
        // children[0] = Input keyword
        // children[1] = (
        // children[2] = VarName
        // children[3] = )
        
        String varName = children.get(2).getLabel().getValue();
        String varReg = getOrCreateVariable(varName);
        
        // Call scanf
        String formatReg = newRegister();
        String secReg = newRegister();
        llvmCode.append("  ").append(formatReg).append(" = getelementptr inbounds [3 x i8], [3 x i8]* @.str_read, i32 0, i32 0\n");
        llvmCode.append("  ").append(secReg).append(" = call i32 (i8*, ...) @scanf(i8* ").append(formatReg).append(", i32* ").append(varReg).append(")\n");
    }
}
