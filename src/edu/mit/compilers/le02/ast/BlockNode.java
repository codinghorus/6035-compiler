package edu.mit.compilers.le02.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import edu.mit.compilers.le02.SourceLocation;
import edu.mit.compilers.le02.symboltable.SymbolTable;

public final class BlockNode extends StatementNode {
  private List<VarDeclNode> decls;
  private List<StatementNode> statements;
  private SymbolTable localSymbolTable;

  public BlockNode(SourceLocation sl,
                   List<VarDeclNode> decls, List<StatementNode> statements) {
    super(sl);
    this.decls = decls;
    this.statements = statements;
  }

  @Override
  public List<ASTNode> getChildren() {
    List<ASTNode> children = new ArrayList<ASTNode>();
    children.addAll(decls);
    children.addAll(statements);
    return children;
  }

  @Override
  public boolean replaceChild(ASTNode prev, ASTNode next) {
    for (ListIterator<VarDeclNode> iter = decls.listIterator();
        iter.hasNext();) {
      if ((iter.next() == prev) && (next instanceof VarDeclNode)) {
        next.setParent(this);
        iter.set((VarDeclNode)next);
        return true;
      }
    }

    for (ListIterator<StatementNode> iter = statements.listIterator();
        iter.hasNext();) {
      if ((iter.next() == prev) && (next instanceof StatementNode)) {
        next.setParent(this);
        iter.set((StatementNode)next);
        return true;
      }
    }
    return false;
  }

  public List<VarDeclNode> getDecls() {
    return decls;
  }

  public void setDecls(List<VarDeclNode> decls) {
    this.decls = decls;
  }

  public List<StatementNode> getStatements() {
    return statements;
  }

  public void setStatements(List<StatementNode> statements) {
    this.statements = statements;
  }

  public void setSymbolTable(SymbolTable locals) {
    localSymbolTable = locals;
  }

  @Override
  public SymbolTable getSymbolTable() {
    return localSymbolTable;
  }


  @Override
  public <T> T accept(ASTNodeVisitor<T> v) {
    return v.visit(this);
  }

}
