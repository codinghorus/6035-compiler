package edu.mit.compilers.le02.ast;

import java.util.Collections;
import java.util.List;

import edu.mit.compilers.le02.SourceLocation;
import edu.mit.compilers.le02.Util;

public final class ReturnNode extends StatementNode {
  private ExpressionNode retValue;
  private boolean hasValue;

  public ReturnNode(SourceLocation sl) {
    super(sl);
    this.hasValue = false;
  }

  public ReturnNode(SourceLocation sl, ExpressionNode retValue) {
    super(sl);
    this.retValue = retValue;
    this.hasValue = true;
  }

  @Override
  public List<ASTNode> getChildren() {
    if (!hasValue) {
      return Collections.emptyList();
    }
    return Util.makeList((ASTNode)retValue);
  }

  @Override
  public boolean replaceChild(ASTNode prev, ASTNode next) {
    if (hasValue && (retValue == prev) && (next instanceof ExpressionNode)) {
      retValue = (ExpressionNode)next;
      retValue.setParent(this);
      return true;
    }
    return false;
  }

  public boolean hasValue() {
    return hasValue;
  }

  public ExpressionNode getRetValue() {
    return retValue;
  }

  public void setRetValue(ExpressionNode retValue) {
    this.retValue = retValue;
  }

  @Override
  public <T> T accept(ASTNodeVisitor<T> v) {
    return v.visit(this);
  }
}
