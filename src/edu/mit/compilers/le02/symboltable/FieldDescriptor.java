package edu.mit.compilers.le02.symboltable;

import edu.mit.compilers.le02.DecafType;
import edu.mit.compilers.le02.GlobalLocation;

public class FieldDescriptor extends TypedDescriptor {

  public FieldDescriptor(SymbolTable parent, String id, DecafType type) {
    super(parent, id, type);
    this.location = new GlobalLocation(id);
  }

  @Override
  public String toString() {
    return this.getType().toString();
  }

}
