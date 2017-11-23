package ram.tutorial.editor.outline;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ram.tutorial.editor.VGLEditor;
import ram.tutorial.editor.parser.ASTFunctionCall;
import ram.tutorial.editor.parser.ASTFunctionDef;
import ram.tutorial.editor.parser.ASTLine;
import ram.tutorial.editor.parser.ASTPoint;
import ram.tutorial.editor.parser.ASTStart;
import ram.tutorial.editor.parser.SimpleNode;


public class VGLOutlineContentProvider implements ITreeContentProvider {
	
	public static final Object[] emptyArray = new Object[]{};
	
	public Object[] getChildren(Object parentElement) {
		
		if (parentElement instanceof OutlineNode)
		{
			return getChildOutlineNodes(((OutlineNode) parentElement).node);
		}
		return emptyArray;
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof OutlineNode)
		{
			return getChildOutlineNodes(((OutlineNode) element).node).length > 0 ? true : false;
		}
		
		return false;
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof VGLEditor)
		{
			VGLEditor editor = (VGLEditor) inputElement;
			ASTStart startNode = editor.getStartNode();
			return getChildOutlineNodes(startNode);
		} else if (inputElement instanceof SimpleNode)
		{
			return getChildOutlineNodes((SimpleNode) inputElement);
		}
		return emptyArray;
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		
	}
	
	//gets children of AST node and returns array of OutlineNode
	private Object[] getChildOutlineNodes(SimpleNode node)
	{
		if (node == null || node.jjtGetNumChildren() == 0)
			return emptyArray;
		
		ArrayList<OutlineNode> outlineNodes = new ArrayList<OutlineNode>();
		
		for (int i = 0; i < node.jjtGetNumChildren(); i++)
		{
			SimpleNode childNode = (SimpleNode)node.jjtGetChild(i);
			
			OutlineNode  oNode = new OutlineNode();
			oNode.node = childNode;
			
			if (childNode instanceof ASTPoint)
			{
				oNode.nodeText = ((ASTPoint)childNode).getPointName();
				oNode.nodeType = OutlineNode.POINT_TYPE;
			} else if (childNode instanceof ASTLine)
			{
				oNode.nodeText = ((ASTLine)childNode).getLineName();
				if (oNode.nodeText == null)
					oNode.nodeText = "Line";
				oNode.nodeType = OutlineNode.LINE_TYPE;
			} else if (childNode instanceof ASTFunctionDef)
			{
				oNode.nodeText = ((ASTFunctionDef)childNode).getFunctionName();
				oNode.nodeType = OutlineNode.FUNCTION_DEF_TYPE;
			} else 
			{
				oNode.nodeText = childNode.jjtGetFirstToken().image;
				if (childNode instanceof ASTFunctionCall)
					oNode.nodeType = OutlineNode.FUNCTION_CALL_TYPE;
				else
					oNode.nodeType = OutlineNode.UNKNOWN_TYPE;
			}
			
			outlineNodes.add(oNode);
		}
		
		return outlineNodes.toArray();
	}
}
