package ram.tutorial.editor.parser;

import java.util.ArrayList;
import java.util.Stack;

/**
 * 
 * @author Ram Kulkarni (rakulkar@adobe.com)
 *
 */
public class VGLParserBase implements VGLParserTreeConstants, VGLParserConstants {
	
	Stack<SimpleNode> nodeStack = new Stack<SimpleNode>();
	
	ArrayList<ASTFunctionDef> functions = new ArrayList<ASTFunctionDef>();
	ArrayList<SimpleNode> variables = new ArrayList<SimpleNode>();  
	
	protected void jjtreeOpenNodeScope(SimpleNode astNode)
	{
		nodeStack.push(astNode);
	}

	protected void jjtreeCloseNodeScope(SimpleNode astNode)
	{
		if (nodeStack.size() > 0)
			nodeStack.pop();
		
		if (astNode instanceof ASTFunctionDef)
			addFunction((ASTFunctionDef) astNode);
		else if (astNode instanceof ASTPoint)
			addVariable(astNode);
	}
	
	protected SimpleNode getCurrentNode()
	{
		if (nodeStack.size() > 0)
			return nodeStack.peek();
		return null;
	}
	
	void addFunction(ASTFunctionDef function)
	{
		functions.add(function);
	}
	
	public ArrayList<ASTFunctionDef> getFunctions()
	{
		return functions;
	}
	
	void addVariable (SimpleNode varNode)
	{
		variables.add(varNode);
	}
	
	public ArrayList<SimpleNode> getVriables()
	{
		return variables;
	}
}
