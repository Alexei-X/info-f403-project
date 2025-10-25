import java.util.List;
import java.util.ArrayList;

/**
 * Parser for the language using recursive descent.
 */
public class Parser {
    private List<Symbol> tokens;
    private int pos = 0;

    public Parser(List<Symbol> tokens) {
        this.tokens = tokens;
        // Add EOS at the end if not present
        if (tokens.isEmpty() || tokens.get(tokens.size()-1).getType() != LexicalUnit.EOS) {
            tokens.add(new Symbol(LexicalUnit.EOS));
        }
    }

    private Symbol current() {
        return tokens.get(pos);
    }

    private void advance() {
        pos++;
    }

    private boolean match(LexicalUnit lu) {
        if (current().getType() == lu) {
            advance();
            return true;
        }
        return false;
    }

    private void expect(LexicalUnit lu) throws ParseException {
        if (!match(lu)) {
            throw new ParseException("Expected " + lu + " but found " + current().getType() + " at line " + current().getLine());
        }
    }

    public ProgramNode parseProgram() throws ParseException {
        expect(LexicalUnit.PROG);
        Symbol progName = current();
        expect(LexicalUnit.PROGNAME);
        expect(LexicalUnit.IS);
        List<InstructionNode> code = parseCode();
        expect(LexicalUnit.END);
        expect(LexicalUnit.EOS);
        return new ProgramNode((String) progName.getValue(), code);
    }

    private List<InstructionNode> parseCode() throws ParseException {
        List<InstructionNode> instructions = new ArrayList<>();
        while (!isEndOfCode()) {
            instructions.add(parseInstruction());
            if (!isEndOfCode()) {
                expect(LexicalUnit.SEMI);
            }
        }
        return instructions;
    }

    private boolean isEndOfCode() {
        LexicalUnit type = current().getType();
        return type == LexicalUnit.END || type == LexicalUnit.ELSE || type == LexicalUnit.EOS;
    }

    private InstructionNode parseInstruction() throws ParseException {
        if (current().getType() == LexicalUnit.VARNAME) {
            return parseAssign();
        } else if (match(LexicalUnit.IF)) {
            return parseIf();
        } else if (match(LexicalUnit.WHILE)) {
            return parseWhile();
        } else if (match(LexicalUnit.PRINT)) {
            return parseOutput();
        } else if (match(LexicalUnit.INPUT)) {
            return parseInput();
        } else {
            throw new ParseException("Unexpected token in instruction: " + current().getType() + " at line " + current().getLine());
        }
    }

    private AssignNode parseAssign() throws ParseException {
        Symbol var = current();
        expect(LexicalUnit.VARNAME);
        expect(LexicalUnit.ASSIGN);
        ExprNode expr = parseExprArith();
        return new AssignNode((String) var.getValue(), expr);
    }

    private IfNode parseIf() throws ParseException {
        expect(LexicalUnit.LBRACK);
        CondNode cond = parseCond();
        expect(LexicalUnit.RBRACK);
        expect(LexicalUnit.THEN);
        List<InstructionNode> thenCode = parseCode();
        IfTailNode tail = parseIfTail();
        return new IfNode(cond, thenCode, tail);
    }

    private IfTailNode parseIfTail() throws ParseException {
        if (match(LexicalUnit.ELSE)) {
            List<InstructionNode> elseCode = parseCode();
            expect(LexicalUnit.END);
            return new IfTailNode(elseCode);
        } else {
            expect(LexicalUnit.END);
            return new IfTailNode(null);
        }
    }

    private WhileNode parseWhile() throws ParseException {
        expect(LexicalUnit.LBRACK);
        CondNode cond = parseCond();
        expect(LexicalUnit.RBRACK);
        expect(LexicalUnit.DO);
        List<InstructionNode> code = parseCode();
        expect(LexicalUnit.END);
        return new WhileNode(cond, code);
    }

    private OutputNode parseOutput() throws ParseException {
        expect(LexicalUnit.LPAREN);
        Symbol var = current();
        expect(LexicalUnit.VARNAME);
        expect(LexicalUnit.RPAREN);
        return new OutputNode((String) var.getValue());
    }

    private InputNode parseInput() throws ParseException {
        expect(LexicalUnit.LPAREN);
        Symbol var = current();
        expect(LexicalUnit.VARNAME);
        expect(LexicalUnit.RPAREN);
        return new InputNode((String) var.getValue());
    }

    private ExprNode parseExprArith() throws ParseException {
        ExprNode left = parseTerm();
        while (current().getType() == LexicalUnit.PLUS || current().getType() == LexicalUnit.MINUS) {
            LexicalUnit op = current().getType();
            advance();
            ExprNode right = parseTerm();
            left = new BinaryExprNode(left, op, right);
        }
        return left;
    }

    private CondNode parseCond() throws ParseException {
        ExprNode left = parseExprArith();
        LexicalUnit op = parseRelOp();
        ExprNode right = parseExprArith();
        return new CondNode(left, op, right);
    }

    private LexicalUnit parseRelOp() throws ParseException {
        if (match(LexicalUnit.SMALLER)) {
            return LexicalUnit.SMALLER;
        } else if (match(LexicalUnit.SMALEQ)) {
            return LexicalUnit.SMALEQ;
        } else if (match(LexicalUnit.EQUAL)) {
            return LexicalUnit.EQUAL;
        } else {
            throw new ParseException("Expected relational operator but found " + current().getType() + " at line " + current().getLine());
        }
    }

    private ExprNode parseTerm() throws ParseException {
        ExprNode left = parseFactor();
        while (current().getType() == LexicalUnit.TIMES || current().getType() == LexicalUnit.DIVIDE) {
            LexicalUnit op = current().getType();
            advance();
            ExprNode right = parseFactor();
            left = new BinaryExprNode(left, op, right);
        }
        return left;
    }

    private ExprNode parseFactor() throws ParseException {
        if (current().getType() == LexicalUnit.NUMBER) {
            String val = (String) current().getValue();
            advance();
            return new NumberNode(val);
        } else if (current().getType() == LexicalUnit.VARNAME) {
            String name = (String) current().getValue();
            advance();
            return new VarNode(name);
        } else if (match(LexicalUnit.LPAREN)) {
            ExprNode expr = parseExprArith();
            expect(LexicalUnit.RPAREN);
            return expr;
        } else {
            throw new ParseException("Expected factor but found " + current().getType() + " at line " + current().getLine());
        }
    }

    // AST Node classes
    public static interface ASTNode {}

    public static class ProgramNode implements ASTNode {
        public String progName;
        public List<InstructionNode> code;
        public ProgramNode(String progName, List<InstructionNode> code) {
            this.progName = progName;
            this.code = code;
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Program: ").append(progName).append("\n");
            for (InstructionNode inst : code) {
                sb.append(inst.toString()).append("\n");
            }
            return sb.toString();
        }
    }

    public static interface InstructionNode extends ASTNode {}

    public static class AssignNode implements InstructionNode {
        public String var;
        public ExprNode expr;
        public AssignNode(String var, ExprNode expr) {
            this.var = var;
            this.expr = expr;
        }
        public String toString() {
            return "Assign: " + var + " = " + expr;
        }
    }

    public static class IfNode implements InstructionNode {
        public CondNode cond;
        public List<InstructionNode> thenCode;
        public IfTailNode tail;
        public IfNode(CondNode cond, List<InstructionNode> thenCode, IfTailNode tail) {
            this.cond = cond;
            this.thenCode = thenCode;
            this.tail = tail;
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("If (").append(cond).append(") Then\n");
            for (InstructionNode inst : thenCode) {
                sb.append("  ").append(inst).append("\n");
            }
            sb.append(tail);
            return sb.toString();
        }
    }

    public static class IfTailNode implements ASTNode {
        public List<InstructionNode> elseCode;
        public IfTailNode(List<InstructionNode> elseCode) {
            this.elseCode = elseCode;
        }
        public String toString() {
            if (elseCode == null) {
                return "End";
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Else\n");
                for (InstructionNode inst : elseCode) {
                    sb.append("  ").append(inst).append("\n");
                }
                sb.append("End");
                return sb.toString();
            }
        }
    }

    public static class WhileNode implements InstructionNode {
        public CondNode cond;
        public List<InstructionNode> code;
        public WhileNode(CondNode cond, List<InstructionNode> code) {
            this.cond = cond;
            this.code = code;
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("While (").append(cond).append(") Do\n");
            for (InstructionNode inst : code) {
                sb.append("  ").append(inst).append("\n");
            }
            sb.append("End");
            return sb.toString();
        }
    }

    public static class OutputNode implements InstructionNode {
        public String var;
        public OutputNode(String var) {
            this.var = var;
        }
        public String toString() {
            return "Print(" + var + ")";
        }
    }

    public static class InputNode implements InstructionNode {
        public String var;
        public InputNode(String var) {
            this.var = var;
        }
        public String toString() {
            return "Input(" + var + ")";
        }
    }

    public static interface ExprNode extends ASTNode {}

    public static class BinaryExprNode implements ExprNode {
        public ExprNode left;
        public LexicalUnit op;
        public ExprNode right;
        public BinaryExprNode(ExprNode left, LexicalUnit op, ExprNode right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }
        public String toString() {
            return "(" + left + " " + op + " " + right + ")";
        }
    }

    public static class NumberNode implements ExprNode {
        public String value;
        public NumberNode(String value) {
            this.value = value;
        }
        public String toString() {
            return value;
        }
    }

    public static class VarNode implements ExprNode {
        public String name;
        public VarNode(String name) {
            this.name = name;
        }
        public String toString() {
            return name;
        }
    }

    public static class CondNode implements ASTNode {
        public ExprNode left;
        public LexicalUnit op;
        public ExprNode right;
        public CondNode(ExprNode left, LexicalUnit op, ExprNode right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }
        public String toString() {
            return left + " " + op + " " + right;
        }
    }

    public static class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }
    }
}