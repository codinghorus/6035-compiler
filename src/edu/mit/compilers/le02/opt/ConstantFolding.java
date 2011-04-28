package edu.mit.compilers.le02.opt;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

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
  // This visitor searches the AST for lists of commuting addends or factors
  // If any of the terms are CallNodes, they do not actually commute
  // In these cases, the visitor returns a null list
  private static class CommutingTerms extends ASTNodeVisitor<Boolean> {
    private static CommutingTerms instance;
    private static MathOp curOp;
    private static List<ExpressionNode> termsList;
    private static boolean atTopLevel;
    private static boolean foundCall;
  
    public static CommutingTerms getInstance() {
      if (instance == null) {
        instance = new CommutingTerms();
      }
      return instance;
    }
  
    public static List<ExpressionNode> getTerms(MathOpNode node) {
      curOp = node.getOp();
      if (curOp != MathOp.ADD || curOp != MathOp.MULTIPLY) {
        return null;
      }

      termsList = new ArrayList<ExpressionNode>();
      atTopLevel = false;
      foundCall = false;

      node.accept(getInstance());
      if (foundCall) {
        return null;
      }
      return termsList;
    }

    public static boolean continuesOp(ExpressionNode node) {
      return ((node instanceof MathOpNode) && 
          (((MathOpNode)node).getOp() == curOp));
    }

    @Override
    public Boolean visit(MathOpNode node) {
      boolean oldAtTopLevel = atTopLevel;;

      for (ASTNode child : node.getChildren()) {
        ExpressionNode expr = (ExpressionNode)child;

        if (oldAtTopLevel) {
          if (continuesOp(expr)) {
            atTopLevel = true;
          } else {
            termsList.add(expr);
            atTopLevel = false;
          }
        }

        expr.accept(this);
      }

      atTopLevel = oldAtTopLevel;
      return true;
    }

    @Override
    public Boolean visit(MethodCallNode node) {
      foundCall = true;
      defaultBehavior(node);
      return true;
    }

    @Override
    public Boolean visit(SystemCallNode node) {
      foundCall = true;
      defaultBehavior(node);
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
    List<ExpressionNode> terms = CommutingTerms.getTerms(node);
    if (terms == null) {
      defaultBehavior(node);
      return true;
    }

    List<IntNode> constants = new ArrayList<IntNode>();
    MathOp curOp = node.getOp();
    Iterator<ExpressionNode> iter = terms.iterator();
    ExpressionNode cur;
    while (iter.hasNext()) {
      cur = iter.next();
      if (cur instanceof IntNode) {
        constants.add((IntNode)cur);
        iter.remove();
      }
    }

    if (constants.size() > 1) {
      int eval = 1;
      if (curOp == MathOp.ADD) {
        eval = 0;
      }

      for (IntNode constant : constants) {
        if (curOp == MathOp.ADD) {
          eval += constant.getValue();
        } else {
          eval *= constant.getValue();
        }
      }

      SourceLocation sl = new SourceLocation(CLI.getInputFilename(), -1, -1);
      IntNode newConstant = new IntNode(sl, eval);

      if (terms.isEmpty()) {
        node.getParent().replaceChild(node, newConstant);
      } else {
        terms.add(newConstant);
        iter = terms.iterator();

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

    defaultBehavior(node);
    return true;
  }
}
