/* Generated By:JJTree: Do not edit this line. ASTFunctionDef.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package ram.tutorial.editor.parser;

import ram.tutorial.editor.util.EditorUtil;

public @SuppressWarnings("all")
class ASTFunctionDef extends SimpleNode {

	String functionName = null;

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public ASTFunctionDef(int id) {
		super(id);
	}

	public ASTFunctionDef(VGLParser p, int id) {
		super(p, id);
	}
}
/*
 * JavaCC - OriginalChecksum=3dfe0c0c6d4b853bfb6225b60fd2ebcd (do not edit this
 * line)
 */
