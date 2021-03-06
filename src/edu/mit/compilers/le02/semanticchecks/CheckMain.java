package edu.mit.compilers.le02.semanticchecks;

import edu.mit.compilers.le02.ErrorReporting;
import edu.mit.compilers.le02.SourceLocation;
import edu.mit.compilers.le02.ast.ASTNode;
import edu.mit.compilers.le02.ast.ClassNode;
import edu.mit.compilers.le02.ast.MethodDeclNode;
import edu.mit.compilers.le02.stgenerator.SymbolTableException;
import edu.mit.compilers.le02.symboltable.MethodDescriptor;
import edu.mit.compilers.le02.symboltable.SymbolTable;
import edu.mit.compilers.le02.symboltable.SymbolTable.SymbolType;

public class CheckMain {
  /** Holds the CheckMain singleton. */
  private static CheckMain instance;

  /**
   * Retrieves the CheckMain singleton, creating if necessary.
   */
  public static CheckMain getInstance() {
    if (instance == null) {
      instance = new CheckMain();
    }
    return instance;
  }

  /**
   * Checks that every identifier is declared before it is used.
   */
  public static void check(ASTNode root) {
    assert(root instanceof ClassNode);

    SymbolTable symbolTable = ((ClassNode)root).getDesc().getSymbolTable();
    if (!symbolTable.contains("main", SymbolType.METHOD)) {
      ErrorReporting.reportError(
        new SymbolTableException(root.getSourceLoc(), "No main method found"));
    } else {
      MethodDescriptor mainDesc = symbolTable.getMethod("main");
      if (mainDesc.getParams().size() > 0) {
        SourceLocation sl = root.getSourceLoc();
        String message = "Main method takes a parameter";
        for (MethodDeclNode method : ((ClassNode)root).getMethods()) {
          if (method.getName().equals("main")) {
            sl = method.getParams().get(0).getSourceLoc();
            message += " " + method.getParams().get(0).getName();
            break;
          }
        }
        ErrorReporting.reportError(new SymbolTableException(sl, message));
      }
    }
  }
}
