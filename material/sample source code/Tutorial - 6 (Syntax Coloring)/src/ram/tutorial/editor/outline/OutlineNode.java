package ram.tutorial.editor.outline;

import ram.tutorial.editor.parser.SimpleNode;
import ram.tutorial.editor.parser.Token;

public class OutlineNode {
	public static final int UNKNOWN_TYPE = 0;
	public static final int POINT_TYPE = 1;
	public static final int LINE_TYPE = 2;
	public static final int FUNCTION_DEF_TYPE = 3;
	public static final int FUNCTION_CALL_TYPE = 4;
	
	public String nodeText = null;
	public int nodeType = UNKNOWN_TYPE;
	public SimpleNode node = null;
	
	public String toString()
	{
		return nodeText;
	}
	
	public boolean equals(Object obj)
	{
		if (obj instanceof OutlineNode == false)
			return false;
		
		OutlineNode argNode = (OutlineNode) obj;
		
		if (node != null && argNode.node != null)
		{
			Token tk1 = node.jjtGetFirstToken();
			Token tk2 = argNode.node.jjtGetFirstToken();
			
			if (tk1.beginLine == tk2.beginLine && tk1.beginColumn == tk2.beginColumn)
				return true;
		}
		return false;
	}
	
	public int hashCode()
	{
		try
		{
			Token tk1 = node.jjtGetFirstToken();
			StringBuffer buf = new StringBuffer();
			buf.append(tk1.beginLine);
			buf.append(tk1.beginColumn);
			return Integer.parseInt(buf.toString());
		} catch (Exception e)
		{
			return super.hashCode();
		}
	}
}
