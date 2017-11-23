package ram.tutorial.editor.util;

import java.io.StringReader;
import java.util.ArrayList;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import ram.tutorial.editor.codeassist.CodeAssistContext;
import ram.tutorial.editor.parser.ASTStart;
import ram.tutorial.editor.parser.SimpleNode;
import ram.tutorial.editor.parser.Token;
import ram.tutorial.editor.parser.VGLParser;

/**
 * 
 * @author Ram Kulkarni (rakulkar@adobe.com)
 *
 */
public class EditorUtil {

	public static ASTStart parseDocument (IDocument doc)
	{
		try
		{
			String docText = doc.get();
			
			VGLParser vglParser = new VGLParser(new StringReader(docText));
			
			return (ASTStart) vglParser.Start();
		} catch (Exception e)
		{
			//e.printStackTrace();
			return null;
		}
	}	
	
	public static SimpleNode getNodeAt (int docOffset, ASTStart startNode, IDocument doc, CodeAssistContext ctx)
	{
		try
		{
			int lineNum = doc.getLineOfOffset(docOffset);
			int lineBeingOffset = doc.getLineOffset(lineNum);
			int offsetInLine = docOffset - lineBeingOffset;
		
			//get trimmed offset
			String lineText = getLineText(doc, docOffset);
			int trimmedLineOffset = offsetInLine;
			for (int i = offsetInLine-1; i >= 0; i--)
			{
				if (Character.isWhitespace(lineText.charAt(i)))
					--trimmedLineOffset;
				else
					break;
			}
			
			//IDocument returns line numbers and offset starting from 0
			//but AST has line and column numbers from 1
			lineNum++;
			
			SimpleNode nodeAtOffset =  getNodeAt(lineNum, trimmedLineOffset, startNode);
			
			if (ctx != null)
			{
				ctx.lineNum = lineNum;
				ctx.lineText = lineText;
				ctx.offsetInLine = offsetInLine;
				ctx.trimmedOffsetInLine = trimmedLineOffset;
			}

			if (nodeAtOffset instanceof ASTStart)
				return null;

			if (ctx != null)
				ctx.nodeAtOffset = nodeAtOffset;
			
			return nodeAtOffset;
		} catch (Exception e)
		{
			
		}
		return null;
	}
	
	public static SimpleNode getNodeAt (int lineNum, int offsetInLine, SimpleNode node)
	{
		//first check children
		if (node.jjtGetNumChildren() > 0)
		{
			for (int i = 0; i < node.jjtGetNumChildren(); i++)
			{
				SimpleNode nodeAtOffset = getNodeAt(lineNum, offsetInLine, (SimpleNode)node.jjtGetChild(i));
				if (nodeAtOffset != null)
					return nodeAtOffset;
			}
		}
		
		return isOffsetInNode(lineNum, offsetInLine, node) ? node : null;
	}
	
	public static boolean isOffsetInNode (int lineNum, int offsetInLine, SimpleNode node)
	{
		Token[] tokens = getStartEndTokens(node);
		
		if (tokens == null)
			return false;
		
		Token startToken = tokens[0];
		Token endToken = tokens[1];
				
		if (lineNum >= startToken.beginLine && offsetInLine >= startToken.beginColumn && 
				lineNum <= endToken.endLine && offsetInLine <= endToken.endColumn)
			return true;
		
		return false;
	}
	
	public static boolean isOffsetInToken (int lineNum, int offsetInLine, Token token)
	{
		if (token == null)
			return false;
		
		if (lineNum == token.beginLine && offsetInLine >= token.beginColumn && offsetInLine <= token.endColumn)
			return true;
		return false;
	}
	
	public static String getNodeText (SimpleNode node)
	{
		Token[] tokens = getStartEndTokens(node);
		
		if (tokens == null)
			return null;
		
		Token startToken = tokens[0];
		Token endToken = tokens[1];
		
		StringBuffer buf = new StringBuffer();
		
		Token currentToken = startToken;
		
		while (currentToken != null && currentToken.compareTo(endToken) <= 0)
		{
			buf.append(currentToken.image);
			currentToken = currentToken.next;
		}
		
		return buf.toString();
	}
	
	public static String getLineText (IDocument doc, int docOffset)
	{
		try
		{
			IRegion lineRegion = doc.getLineInformationOfOffset(docOffset);
			return doc.get(lineRegion.getOffset(), lineRegion.getLength());
		} catch (Exception e)
		{
			return null;
		}
	}
	
	public static ArrayList<Token> getTokensInNode (SimpleNode node)
	{
		Token[] tokens = getStartEndTokens(node);
		
		if (tokens == null)
			return null;
		
		Token startToken = tokens[0];
		Token endToken = tokens[1];
		
		ArrayList<Token> result = new ArrayList<Token>();
		
		Token currentToken = startToken;
		
		while (currentToken != null && currentToken.compareTo(endToken) <= 0)
		{
			result.add(currentToken);
			currentToken = currentToken.next;
		}
		
		return result;
	}
	
	private static Token[] getStartEndTokens (SimpleNode node)
	{
		Token startToken = node.jjtGetFirstToken();
		Token endToken = node.jjtGetLastToken();
		
		if (startToken == null && endToken == null)
			return null;

		if (startToken == null && endToken != null)
			startToken = endToken;
		else if (endToken == null && startToken != null)
			endToken = startToken;

		if (endToken.compareTo(startToken) < 0)
			return null;
		
		return new Token[] {startToken, endToken};
	}
}
