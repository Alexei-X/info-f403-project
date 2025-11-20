package LexicalAnalyzer;

/**
 *  Enumeration of non-terminal symbols used in the grammar.
 *  Each constant corresponds to a production rule in the parser.
 */
public enum NonTermUnit {
    /** {@code <Program>} */
    Program,
    /** {@code <Code>} */
    Code,
    /** {@code <Instruction>} */
    Instruction,
    /** {@code <Assign>} */
    Assign,
    /** {@code <If>} */
    If,
    /** {@code <While>} */
    While,
    /** {@code <Call>} */
    Call,
    /** {@code <Output>} */
    Output,
    /** {@code <Input>} */
    Input,
    /** {@code <ExprArith>} */
    ExprArith,
    /** {@code <Prod>} */
    Prod,
    /** {@code <ExprArithp>} */
    ExprArithp,
    /** {@code <Prodp>} */
    Prodp,
    /** {@code <Atom>} */
    Atom,
    /** {@code <Cond>} */
    Cond,
    /** {@code <C>} */
    C,
    /** {@code <CondA>} */
    CondA,
    /** {@code <CondB>} */
    CondB,
    /** {@code <D>} */
    D,
} 
