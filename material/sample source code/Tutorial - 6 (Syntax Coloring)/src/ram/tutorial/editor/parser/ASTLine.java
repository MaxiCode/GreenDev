/* Generated By:JJTree: Do not edit this line. ASTLine.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package ram.tutorial.editor.parser;

public @SuppressWarnings("all")
class ASTLine extends SimpleNode {
	String lineName = null;

	public String getLineName() {
		return lineName;
	}

	public void setLineName(String lineName) {
		this.lineName = lineName;
	}

	public ASTLine(int id) {
		super(id);
	}

	public ASTLine(VGLParser p, int id) {
		super(p, id);
	}

}
/*
 * JavaCC - OriginalChecksum=dd5ce7484e4c91b5d9d3aab306081dff (do not edit this
 * line)
 */
