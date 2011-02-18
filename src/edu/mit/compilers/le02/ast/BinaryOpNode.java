package edu.mit.compilers.le02.ast;

import java.util.ArrayList;
import java.util.List;

public abstract class BinaryOpNode extends ExpressionNode {
	protected ExpressionNode left, right;

	public BinaryOpNode(SourceLocation sl) {
		super(sl);
	}
	
	public BinaryOpNode(SourceLocation sl, ExpressionNode left, ExpressionNode right) {
		super(sl);
		this.left = left;
		this.right = right;
	}

	@Override
	public List<ASTNode> getChildren() {
		List<ASTNode> children = new ArrayList<ASTNode>();
		children.add(left);
		children.add(right);
		return children;
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
