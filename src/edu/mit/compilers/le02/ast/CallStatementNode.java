package edu.mit.compilers.le02.ast;

import java.util.ArrayList;
import java.util.List;

import edu.mit.compilers.le02.SourceLocation;

public final class CallStatementNode extends StatementNode {
  private CallNode call;

  public CallStatementNode(SourceLocation sl, CallNode call) {
    super(sl);
    this.call = call;
  }

  @Override
  public List<ASTNode> getChildren() {
    List<ASTNode> children = new ArrayList<ASTNode>();
    children.add(call);
    return children;
  }

  @Override
  public boolean replaceChild(ASTNode prev, ASTNode next) {
    if ((call == prev) && (next instanceof CallNode)) {
      call = (CallNode)next;
      call.setParent(this);
      return true;
    }
    return false;
  }

  public CallNode getCall() {
    return call;
  }


  public void setCall(CallNode call) {
    this.call = call;
  }


  @Override
  public <T> T accept(ASTNodeVisitor<T> v) {
    return v.visit(this);
  }

}
