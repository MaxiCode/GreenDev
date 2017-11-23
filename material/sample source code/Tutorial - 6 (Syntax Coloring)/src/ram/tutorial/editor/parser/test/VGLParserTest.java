package ram.tutorial.editor.parser.test;

import java.io.FileInputStream;

import ram.tutorial.editor.parser.SimpleNode;
import ram.tutorial.editor.parser.VGLParser;

/**
 * 
 * @author Ram Kulkarni (rakulkar@adobe.com)
 *
 */
public class VGLParserTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String filePath = "C:/Users/rkulkarni/projects/ColdFusion/IDE/Workspaces/EditorTutorialWorkspace/Tutorial - 3/src/ram/tutorial/editor/parser/test/test.vgl";

		FileInputStream fileStream = new FileInputStream(filePath);
		
		VGLParser parser = new VGLParser(fileStream);
		
		SimpleNode node = parser.Start();
		
		node.dump(">");
	}

}
