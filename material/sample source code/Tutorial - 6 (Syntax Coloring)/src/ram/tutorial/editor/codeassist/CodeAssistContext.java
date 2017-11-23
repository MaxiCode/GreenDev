package ram.tutorial.editor.codeassist;

import ram.tutorial.editor.parser.SimpleNode;
import ram.tutorial.editor.parser.VGLParser;

/**
 * 
 * @author Ram Kulkarni (rakulkar@adobe.com)
 *
 */
public class CodeAssistContext {
	public int docOffset;
	public int lineNum; //starts from 1
	public int offsetInLine; //starts from 1
	public int trimmedOffsetInLine; //starts from 1
	public String lineText = null;
	public SimpleNode nodeAtOffset;
	public String docText;
	public VGLParser parser = null;
}
