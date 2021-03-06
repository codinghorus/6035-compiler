package edu.mit.compilers.le02.ast;

import java.util.Collections;
import java.util.List;

import edu.mit.compilers.le02.DecafType;
import edu.mit.compilers.le02.SourceLocation;

public final class ArrayDeclNode extends FieldDeclNode {
  private int length;

  public ArrayDeclNode(SourceLocation sl,
                       DecafType type, String id, int length) {
    super(sl, type, id);
    this.length = length;
  }

  @Override
  public List<ASTNode> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public boolean replaceChild(ASTNode prev, ASTNode next) {
    return false;
  }

  public int getLength() {
    return length;
  }

  @Override
  public String toString() {
    return super.toString() + " " + type + " " + name + "[" + length + "]";
  }

  @Override
  public <T> T accept(ASTNodeVisitor<T> v) { return v.visit(this); }
}
