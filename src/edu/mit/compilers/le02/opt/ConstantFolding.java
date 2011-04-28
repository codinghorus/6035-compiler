package edu.mit.compilers.le02.opt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import edu.mit.compilers.le02.DecafType;
import edu.mit.compilers.le02.SourceLocation;
import edu.mit.compilers.le02.ast.ASTNode;
import edu.mit.compilers.le02.ast.ASTNodeVisitor;
import edu.mit.compilers.le02.ast.CallNode;
import edu.mit.compilers.le02.ast.ExpressionNode;
import edu.mit.compilers.le02.ast.IntNode;
import edu.mit.compilers.le02.ast.MathOpNode;
import edu.mit.compilers.le02.ast.MathOpNode.MathOp;
import edu.mit.compilers.le02.ast.MethodCallNode;
import edu.mit.compilers.le02.ast.SystemCallNode;
import edu.mit.compilers.tools.CLI;

public class ConstantFolding extends ASTNodeVisitor<Boolean> {
  // This visitor searches a subtree of the AST for method calls
  // and returns true if it finds any
  // It is currently unused, but I'll include it in the file for now
  private static class CheckForMethodCalls extends ASTNodeVisitor<Boolean> {
    private static CheckForMethodCalls instance;
    private static boolean foundMethodCall;

    public static CheckForMethodCalls getInstance() {
      if (instance == null) {
        instance = new CheckForMethodCalls();
      }
      return instance;
    }

    public static Boolean check(ASTNode node) {
      foundMethodCall = false;
      node.accept(getInstance());
      return foundMethodCall;
    }

    @Override
    public Boolean visit(MethodCallNode node) {
      foundMethodCall = true;
      defaultBehavior(node);
      return true;
    }
  }

  // This visitor searches the AST for the list of all addends or factors
  // of a sum or product expression
  private static class ReconstructTerms extends ASTNodeVisitor<Boolean> {
    private static ReconstructTerms instance;
    private static MathOp curOp;
    private static List<ExpressionNode> termsList;
  
    public static ReconstructTerms getInstance() {
      if (instance == null) {
        instance = new ReconstructTerms();
      }
      return instance;
    }
  
    public static List<ExpressionNode> getTerms(MathOpNode node) {
      termsList = new ArrayList<ExpressionNode>();
      curOp = node.getOp();
      node.accept(getInstance());
      return termsList;
    }

    public static boolean continuesOp(ExpressionNode node) {
      return ((node instanceof MathOpNode) && 
          (((MathOpNode)node).getOp() == curOp));
    }

    @Override
    public Boolean visit(MathOpNode node) {
      List<ExpressionNode> children =
          new ArrayList<ExpressionNode>(
          Arrays.asList(node.getLeft(), node.getRight()));

      for (ASTNode child : node.getChildren()) {
        ExpressionNode expr = (ExpressionNode)child;

        if (continuesOp(expr)) {
          expr.accept(this);
        } else {
          termsList.add(expr);
        }
      }

      return true;
    }
  }

  private static ConstantFolding instance;

  public static ConstantFolding getInstance() {
    if (instance == null) {
      instance = new ConstantFolding();
    }
    return instance;
  }

  public static void foldConstants(ASTNode root) {
    assert(root instanceof CallNode);
    root.accept(getInstance());
  }

  public Boolean visit(MathOpNode node) {
    MathOp curOp = node.getOp();
    if (curOp != MathOp.ADD && curOp != MathOp.MULTIPLY) {
      // No folding is done on subtraction or division nodes
      // so for now just recurse on the subtree
      defaultBehavior(node);
      return true;
    }

    List<ExpressionNode> terms = ReconstructTerms.getTerms(node);
    List<IntNode> constants = new ArrayList<IntNode>();

    ListIterator<ExpressionNode> iter = terms.listIterator();
    ExpressionNode cur;
    while (iter.hasNext()) {
      cur = iter.next();
      if (cur instanceof IntNode) {
        constants.add((IntNode)cur);
        iter.remove();
      }
    }

    if (constants.size() < 2) {
      defaultBehavior(node);
      return true;
    }

    int eval;
    if (curOp == MathOp.ADD) {
      eval = 0;
    } else {
      eval = 1;
    }

    for (IntNode constant : constants) {
      if (curOp == MathOp.ADD) {
        eval += constant.getValue();
      } else {
        eval *= constant.getValue();
      }
    }

    SourceLocation sl = new SourceLocation(CLI.getInputFilename(),
        constants.get(0).getSourceLoc().getLine(), -1);
    IntNode newConstant = new IntNode(sl, eval);

    if (terms.isEmpty()) {
      node.getParent().replaceChild(node, newConstant);
    } else {
      terms.add(newConstant);
      iter = terms.listIterator();

      MathOpNode newNode = 
          new MathOpNode(sl, iter.next(), iter.next(), curOp);
      while (iter.hasNext()) {
        newNode = new MathOpNode(sl, newNode, iter.next(), curOp);
      }

      node.getParent().replaceChild(node, newNode);
      for (ExpressionNode child : terms) {
        child.accept(this);
      }
    }

    return true;
  }
}
