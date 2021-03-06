package edu.mit.compilers.le02.ast;

import java.util.List;

import edu.mit.compilers.le02.SourceLocation;
import edu.mit.compilers.le02.Util;

public abstract class BinaryOpNode extends ExpressionNode {
  protected ExpressionNode left, right;

  public BinaryOpNode(SourceLocation sl) {
    super(sl);
  }

  public BinaryOpNode(SourceLocation sl, ExpressionNode left,
                      ExpressionNode right) {
    super(sl);
    this.left = left;
    this.right = right;
  }

  @Override
  public List<ASTNode> getChildren() {
    return Util.makeList((ASTNode)left, right);
  }

  @Override
  public boolean replaceChild(ASTNode prev, ASTNode next) {
    if ((left == prev) && (next instanceof ExpressionNode)) {
      left = (ExpressionNode)next;
      left.setParent(this);
      return true;
    } else if ((right == prev) && (next instanceof ExpressionNode)) {
      right = (ExpressionNode)next;
      right.setParent(this);
      return true;
    }
    return false;
  }

  public ExpressionNode getLeft() {
    return left;
  }

  public void setLeft(ExpressionNode left) {
    this.left = left;
  }

  public ExpressionNode getRight() {
    return right;
  }

  public void setRight(ExpressionNode right) {
    this.right = right;
  }

}
