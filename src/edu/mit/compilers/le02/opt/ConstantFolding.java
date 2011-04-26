package edu.mit.compilers.le02.semanticchecks;

import java.util.ArrayList;
import java.util.List;

import edu.mit.compilers.le02.DecafType;
import edu.mit.compilers.le02.ast.ASTNode;
import edu.mit.compilers.le02.ast.ASTNodeVisitor;
import edu.mit.compilers.le02.ast.CallNode;
import edu.mit.compilers.le02.ast.ExpressionNode;
import edu.mit.compilers.le02.ast.MathOpNode;
import edu.mit.compilers.le02.ast.MathOpNode.MathOp;

public class ConstantFolding extends ASTNodeVisitor<Boolean> {
  // This visitor searches the AST for lists of commuting addends or factors
  // If any of the terms are CallNodes, they do not actually commute
  // In these cases, the visitor returns a null list
  private static class CommutingTerms extends ASTNodeVisitor<Boolean> {
    private static CommutingTerms instance;
    private static MathOp curOp;
    private static List<ExpressionNode> termsList;
  
    public static CommutingTerms getInstance() {
      if (instance == null) {
        instance = new CommutingTerms();
      }
      return instance;
    }
  
    public static List<ExpressionNode> getTermsList(ASTNode root) {
      assert(root instanceof MathOpNode);

      curOp = ((MathOpNode)root).getOp();
      assert(curOp == MathOp.ADD || curOp == MathOp.MULTIPLY);
      termsList = new ArrayList<ExpressionNode>();

      boolean foundCall = root.accept(getInstance());
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
      boolean foundCall = false;

      for (ASTNode child : node.getChildren()) {
        ExpressionNode expr = (ExpressionNode)child;

        if (continuesOp(expr)) {
          foundCall = expr.accept(this) || foundCall;
        } else {
          termsList.add(expr);
          if (expr instanceof CallNode) {
            foundCall = true;
          }
        }
      }

      return foundCall;
    }
  }

  private static ConstantFolding instance;

  public static ConstantFolding getInstance() {
    if (instance == null) {
      instance = new ConstantFolding();
    }
    return instance;
  }

  public static void constantFolding(ASTNode root) {
    assert(root instanceof CallNode);
    root.accept(getInstance());
  }
}
