package ram.tutorial.editor;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import ram.tutorial.editor.parser.ASTButton;
import ram.tutorial.editor.parser.ASTCanvas;
import ram.tutorial.editor.parser.ASTCircle;
import ram.tutorial.editor.parser.ASTFunctionCall;
import ram.tutorial.editor.parser.ASTFunctionDef;
import ram.tutorial.editor.parser.ASTLine;
import ram.tutorial.editor.parser.ASTPoint;
import ram.tutorial.editor.parser.SimpleNode;
import ram.tutorial.editor.parser.Token;

/**
 * 
 * @author Ram Kulkarni (rakulkar@adobe.com)
 *
 */
public class VGLColorizer {

	private static final VGLColorizer instance = new VGLColorizer();
	
	Color commandColor, keywordColor, functionCallColor;
	
	private VGLColorizer()
	{
		loadColors();
	}
	
	protected void finalize()
	{
		if (commandColor != null)
			commandColor.dispose();
		if (keywordColor != null)
			keywordColor.dispose();
		if (functionCallColor != null)
			functionCallColor.dispose();
	}
	
	public void loadColors()
	{
		Display display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		
		commandColor = new Color(display, 255,0,0);
		keywordColor = new Color(display, 0,255,0);
		functionCallColor = new Color (display, 0,0,255);
	}
	
	public static VGLColorizer getInstance()
	{
		return instance;
	}
	
	public void colorizeLine (int lineNum, int lineDocOffset, SimpleNode node, ArrayList<StyleRange> styles)
	{
		//process this node first and then children
		
		Token startToken = node.jjtGetFirstToken();
		if (startToken.beginLine == lineNum)
			colorizeNode(node, lineDocOffset, styles);
		
		for (int i = 0; i < node.jjtGetNumChildren(); i++)
			colorizeLine(lineNum, lineDocOffset, (SimpleNode)node.jjtGetChild(i), styles);
	}
	
	private void colorizeNode (SimpleNode node, int lineDocOffset, ArrayList<StyleRange> styles)
	{
		Token startToken = node.jjtGetFirstToken();
		
		Color color = null;
		int fontStyle = SWT.NORMAL;
		
		if (node instanceof ASTPoint || node instanceof ASTLine || node instanceof ASTCanvas ||
				node instanceof ASTCircle || node instanceof ASTButton)
		{
			color = commandColor;
			fontStyle = SWT.BOLD;
		} else if (node instanceof ASTFunctionCall)
		{
			color = functionCallColor;
		} else if (node instanceof ASTFunctionDef)
		{
			color = keywordColor;
			fontStyle = SWT.BOLD;
		}
		
		StyleRange style = new StyleRange(lineDocOffset + startToken.beginColumn-1, startToken.image.length(), color, null);
		style.fontStyle = SWT.BOLD;
		styles.add(style);
	}
}
