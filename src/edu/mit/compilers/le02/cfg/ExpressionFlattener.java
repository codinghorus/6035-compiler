package edu.mit.compilers.le02.cfg;

import java.util.ArrayList;
import java.util.List;

import edu.mit.compilers.le02.DecafType;
import edu.mit.compilers.le02.ast.ASTNode;
import edu.mit.compilers.le02.ast.ASTNodeVisitor;
import edu.mit.compilers.le02.ast.ArrayLocationNode;
import edu.mit.compilers.le02.ast.AssignNode;
import edu.mit.compilers.le02.ast.BoolOpNode;
import edu.mit.compilers.le02.ast.BooleanNode;
import edu.mit.compilers.le02.ast.CallStatementNode;
import edu.mit.compilers.le02.ast.ExpressionNode;
import edu.mit.compilers.le02.ast.IntNode;
import edu.mit.compilers.le02.ast.MathOpNode;
import edu.mit.compilers.le02.ast.MethodCallNode;
import edu.mit.compilers.le02.ast.MinusNode;
import edu.mit.compilers.le02.ast.NotNode;
import edu.mit.compilers.le02.ast.ReturnNode;
import edu.mit.compilers.le02.ast.ScalarLocationNode;
import edu.mit.compilers.le02.ast.VariableNode;
import edu.mit.compilers.le02.cfg.OpStatement.Op;
import edu.mit.compilers.le02.symboltable.LocalDescriptor;
import edu.mit.compilers.le02.symboltable.Location;
import edu.mit.compilers.le02.symboltable.SymbolTable;

/**
 * This singleton class takes a control flow graph and flattens all the
 * UnexpandedStatements into BasicStatements.  It is ok with having existing
 * BasicStatements in the control flow graph.
 * 
 * The class will add some temporary locations into the symbol tables in order
 * to handle complex expressions with more than one operator.
 * 
 * @author dkoh
 *
 */
public final class ExpressionFlattener extends ASTNodeVisitor<Argument> {
  private static ExpressionFlattener instance = null;
  private List<BasicStatement> statements;
  
  public static ExpressionFlattener getInstance() {
    if (instance == null) {
      instance = new ExpressionFlattener();
    }
    return instance;
  }
  
  public static List<BasicStatement> flatten(UnexpandedStatement us) {
    return getInstance().flattenStatement(us);
  }
  
  public static List<BasicStatement> 
  flattenStatements(List<BasicStatement> statementList) {
    ArrayList<BasicStatement> retList = new ArrayList<BasicStatement>();
    
    for (BasicStatement bs : statementList) {
      retList.addAll(bs.flatten());
    }
    return retList;
  }

  public List<BasicStatement> flattenStatement(UnexpandedStatement us) {
    statements = new ArrayList<BasicStatement>();
    us.getNode().accept(this);
    return statements;
  }
  
  
  private Op convertOp(MathOpNode.MathOp op) {
    switch(op) {
      case ADD:
        return Op.ADD;
      case SUBTRACT:
        return Op.SUBTRACT;
      case MULTIPLY:
        return Op.MULTIPLY;
      case DIVIDE:
        return Op.DIVIDE;
      case MODULO:
        return Op.MODULO;
    }
    assert false;
    return null;
  }
  
  private Op convertOp(BoolOpNode.BoolOp op) {
    switch(op) {
      case LE:
        return Op.LESS_OR_EQUAL;
      case LT:
        return Op.LESS_THAN;
      case GE:
        return Op.GREATER_OR_EQUAL;
      case GT:
        return Op.GREATER_THAN;
      case EQ:
        return Op.EQUAL;
      case NEQ:
        return Op.NOT_EQUAL;
    }
    assert false;
    return null;
  }
  
  private Location makeTemp(ASTNode node) {
    SymbolTable st = node.getSymbolTable();
    
    int nextIndex = st.getLargestLocalOffset() - 8;
    Location loc = new Location();
    loc.setStackLocation("rbp", nextIndex);
    
    LocalDescriptor ld = new LocalDescriptor(st, Math.abs(nextIndex) + "lcltmp", 
        DecafType.INT);
    st.put(ld.getId(), ld, node.getSourceLoc());
    return loc;
  }
  
  /*
   * Statement Visiting Methods
   */
  public Argument visit(AssignNode node) {
    Location destLoc = node.getLoc().getDesc().getLocation();
    Argument dest = Argument.makeArgument(destLoc);
    Argument src = node.getValue().accept(this);
    
    statements.add(new OpStatement(node, Op.RETURN, src, dest, null));
    return null;
  }
  
  public Argument visit(CallStatementNode node) {
    node.getCall().accept(this);
    return null;
  }

  public Argument visit(ReturnNode node) {
    Argument arg1 = null;
    if (node.hasValue()) {
      arg1 = node.getRetValue().accept(this);
    }
    
    statements.add(new OpStatement(node, Op.RETURN, arg1, null, null));
    return null;
  }
  
  /*
   * Expression Visiting Methods
   */  
  public Argument visit(BoolOpNode node) {
    Argument arg1 = node.getLeft().accept(this);
    Argument arg2 = node.getRight().accept(this);
    Location loc = makeTemp(node);
    
    OpStatement s = new OpStatement(node, convertOp(node.getOp()), 
                                    arg1, arg2, loc);
    statements.add(s);
    return Argument.makeArgument(loc);
  }
  
  public Argument visit(MathOpNode node) {
    Argument arg1 = node.getLeft().accept(this);
    Argument arg2 = node.getRight().accept(this);
    Location loc = makeTemp(node);
    
    OpStatement s = new OpStatement(node, convertOp(node.getOp()), 
                                    arg1, arg2, loc);
    statements.add(s);
    return Argument.makeArgument(loc);
  }
  
  public Argument visit(NotNode node) {
    Location loc = makeTemp(node);
    
    OpStatement s = new OpStatement(node, Op.NOT, 
                                    node.getExpr().accept(this), null, loc);
    statements.add(s);
    return Argument.makeArgument(loc);
  }
  
  public Argument visit(MinusNode node) {
    Location loc = makeTemp(node);
    
    OpStatement s = new OpStatement(node, Op.UNARY_MINUS, 
                                    node.getExpr().accept(this), null, loc);
    statements.add(s);
    return Argument.makeArgument(loc);
  }
  
  public Argument visit(MethodCallNode node) {
    Location loc = makeTemp(node);
    
    List<Argument> args = new ArrayList<Argument>();
    for (ExpressionNode n : node.getArgs()) {
      args.add(n.accept(this));
    }
    
    CallStatement s = new CallStatement(node, node.getDesc(), args, loc);
    statements.add(s);
    
    return Argument.makeArgument(loc);
  }
  
  public Argument visit(VariableNode node) {
    return node.getLoc().accept(this);
  }
  
  /*
   * Location and Constant Visiting Methods 
   */
  public Argument visit(ScalarLocationNode node) {
    return Argument.makeArgument(node.getDesc().getLocation());
  }
  
  public Argument visit(ArrayLocationNode node) {
    Argument a = node.getIndex().accept(this);
    return Argument.makeArgument(node.getDesc().getLocation(), a);
  }
  
  public Argument visit(BooleanNode node) {
    return Argument.makeArgument(node.getValue());
  }
  
  public Argument visit(IntNode node) {
    return Argument.makeArgument(node.getValue());
  }

}
