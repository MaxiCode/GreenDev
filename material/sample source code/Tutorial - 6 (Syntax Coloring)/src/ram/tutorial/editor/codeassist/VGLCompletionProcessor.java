package ram.tutorial.editor.codeassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import ram.tutorial.editor.parser.ASTFunctionCall;
import ram.tutorial.editor.parser.ASTFunctionDef;
import ram.tutorial.editor.parser.ASTLine;
import ram.tutorial.editor.parser.ASTPoint;
import ram.tutorial.editor.parser.ASTStart;
import ram.tutorial.editor.parser.SimpleNode;
import ram.tutorial.editor.parser.Token;
import ram.tutorial.editor.util.EditorUtil;

/**
 * 
 * @author Ram Kulkarni (rakulkar@adobe.com)
 *
 */
public class VGLCompletionProcessor implements IContentAssistProcessor {

	private static final char[] autoActivationChars = new char[] {'.',' '}; 

	private static final String[] commands = new String[] {
		"Line", "Point", "Canvas", "Circle"
	};
	
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		
		ASTStart startNode = EditorUtil.parseDocument(viewer.getDocument());
		
		if (startNode == null)
			return null;
		
		CodeAssistContext ctx = new CodeAssistContext();
		ctx.docOffset = offset;
		ctx.docText = viewer.getDocument().get();
		ctx.parser = startNode.getParser();
		
		
		SimpleNode nodeAtOffset = EditorUtil.getNodeAt(offset, startNode, viewer.getDocument(), ctx);

		ArrayList<ICompletionProposal> proposalList = null;
		
		if (nodeAtOffset != null)
			proposalList =  createCompletionProposals(ctx);
		else //node at offset is null
		{
			//if it is beginning of the line then display commands and functions 
			boolean atLineHead = true;
			for (int i = 0; i < ctx.offsetInLine; i++)
			{
				if (!Character.isWhitespace(ctx.lineText.charAt(i)))
				{
					atLineHead = false;
					break;
				}
			}
			
			if (atLineHead)
				proposalList = getProposalsForCommandAndFunctions("", ctx);
		}
		
		if (proposalList != null)
		{
			Collections.sort((List)proposalList);
			ICompletionProposal[] result = new ICompletionProposal[proposalList.size()];
			for (int i = 0; i < proposalList.size(); i++)
				result[i] = proposalList.get(i);
			return result;
		}
		
		return null;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return autoActivationChars;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	ArrayList<ICompletionProposal> createCompletionProposals (CodeAssistContext ctx)
	{
		SimpleNode node = ctx.nodeAtOffset;
		
		ArrayList<ICompletionProposal> proposalList = null;
		
		if (node instanceof ASTFunctionCall)
			proposalList = processFunctionCall(ctx);
		else if (node instanceof ASTLine)
			proposalList = processLine(ctx);
		
		return proposalList;
		
	}

	private ArrayList<ICompletionProposal> processLine(CodeAssistContext ctx) {
		
		//show list of points for the first two arguments only

		//if no points are specified then show all point names
		
		ArrayList<Token> lineTokens = EditorUtil.getTokensInNode(ctx.nodeAtOffset);
		
		//first token is line command
		
		if (lineTokens.size() < 2)
			return getPoints("", ctx); // display all points
		
		
		boolean showPointNames = false;
		
		//check if offset is in the first point token
		Token firstPointToken = lineTokens.get(1);
		if (EditorUtil.isOffsetInToken(ctx.lineNum, ctx.offsetInLine, firstPointToken))
			showPointNames = true;
		
		if (!showPointNames )
		{
			//check if offset is in the second point token
			if (lineTokens.size() > 2)
			{
				Token secondPointToken = lineTokens.get(2);
				if (EditorUtil.isOffsetInToken(ctx.lineNum, ctx.offsetInLine, secondPointToken))
					showPointNames = true;
			} else
				showPointNames = true; //second point is not specified. So show proposals for point
		}
		
		if (showPointNames)
		{
			String prefix = getCodeAssistPrefix(ctx, null);
			return getPoints(prefix, ctx);
		}
		
		return null;
	}
	
	private ArrayList<ICompletionProposal> getPoints (String prefix, CodeAssistContext ctx)
	{
		if (ctx.parser == null)
			return null;
		
		if (prefix == null)
			prefix = "";
		
		prefix = prefix.toLowerCase();
		
		ArrayList<SimpleNode> variables = ctx.parser.getVriables();
		
		if (variables == null || variables.size() == 0)
			return null;
		
		ArrayList<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
		
		for (int i = 0; i < variables.size(); i++)
		{
			SimpleNode node = variables.get(i);
			if (node instanceof ASTPoint == false)
				continue;
			
			String pointName = ((ASTPoint)node).getPointName();
			
			if (pointName == null || !pointName.toLowerCase().startsWith(prefix))
				continue;
				
			VGLCompletionProposal proposal = new VGLCompletionProposal(
					((ASTPoint)node).getPointName(), VGLCompletionProposal.VARIABLE_TYPE);
			result.add(proposal);
		}
		
		return result;
	}

	private ArrayList<ICompletionProposal> processFunctionCall(CodeAssistContext ctx) {
		
		Token firstToken = ctx.nodeAtOffset.jjtGetFirstToken();

		if (!EditorUtil.isOffsetInToken(ctx.lineNum, ctx.offsetInLine, firstToken))
			return null; //code assist for function  names or commands only, which are in the first token only
		
		boolean showCommands = true;
		boolean showFunctions = true;
		String prefix = getCodeAssistPrefix(ctx, null);
		
		if (firstToken != null)
		{
			if (firstToken.next != null && firstToken.next.image.equals("("))
				showCommands = false;
		}
		
		ArrayList<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
		if (showCommands)
		{
			ArrayList<ICompletionProposal> commands = getProposalsForCommand(prefix, ctx);
			if (commands != null)
				result.addAll(commands);
		}
		
		if (showFunctions)
		{
			ArrayList<ICompletionProposal> functions = getProposalsForFunctions(prefix, ctx);
			if (functions != null)
				result.addAll(functions);
		}
		
		return result;
	}
	
	private ArrayList<ICompletionProposal> getProposalsForCommand (String prefix, CodeAssistContext ctx)
	{
		if (prefix == null)
			prefix = "";
		prefix = prefix.toLowerCase();
		ArrayList<String> proposalStrs = new ArrayList<String>();
		for (int i = 0; i < commands.length; i++)
		{
			if (commands[i].toLowerCase().startsWith(prefix))
				proposalStrs.add(commands[i]);
		}
		return createProposals(proposalStrs, VGLCompletionProposal.COMMAND_TYPE);
	}
	
	private ArrayList<ICompletionProposal> getProposalsForCommandAndFunctions (String prefix, CodeAssistContext ctx)
	{
		ArrayList<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
		ArrayList<ICompletionProposal> commands = getProposalsForCommand(prefix, ctx);
		if (commands != null)
			result.addAll(commands);
		
		ArrayList<ICompletionProposal> functions = getProposalsForFunctions(prefix, ctx);
		if (functions != null)
			result.addAll(functions);
		
		return result;
	}
	
	private ArrayList<ICompletionProposal> getProposalsForFunctions(String prefix, CodeAssistContext ctx)
	{
		
		if (ctx.parser == null)
			return null;

		if (prefix == null)
			prefix = "";
		
		prefix = prefix.toLowerCase();

		ArrayList<ASTFunctionDef> functions = ctx.parser.getFunctions();
		if (functions == null || functions.size() == 0)
			return null;
		
		ArrayList<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
		
		for (int i = 0; i < functions.size(); i ++)
		{
			ASTFunctionDef functionDef = functions.get(i);
			String name = functionDef.getFunctionName();
			if (name == null || name.length() == 0)
				continue;
			if (!name.toLowerCase().startsWith(prefix))
				continue;
			VGLCompletionProposal proposal = new VGLCompletionProposal(name, VGLCompletionProposal.FUNCTION_TYPE);
			result.add(proposal);
		}
		
		return result;
	}
	
	private static String getCodeAssistPrefix (CodeAssistContext context, char[] delimiters)
	{
		if (delimiters == null)
			delimiters = new char[] {' '};
		
		Arrays.sort(delimiters);
		
		StringBuffer buf = new StringBuffer();
		
		int startOffset = context.docOffset-1;
		
		if (startOffset >= context.docText.length())
			startOffset = context.docText.length()-1;
		
		for (int i = startOffset; i >= 0; --i)
		{
			char chr = context.docText.charAt(i);
			if (Character.isWhitespace(chr) || Arrays.binarySearch(delimiters, chr)>= 0)
				break;
			buf.append(chr);
		}
		
		String prefix = buf.reverse().toString();

		return getFieldNameFromFunctionCall(prefix);
	}
	
	private static String getFieldNameFromFunctionCall(String fieldName)
	{
		//check if the prefix has (
		int index = fieldName.lastIndexOf('(');
		int index1 = -1;
		String newFieldName = fieldName;
		if (index >= 0)
		{
			//check if there is corresponding )
			index1 = fieldName.indexOf(')', index);
			int fromIndex = index + 1;
			if (index + 1 >= fieldName.length() )
				return ""; //$NON-NLS-1$
			if (index1 > 0)
			{
				newFieldName = fieldName.substring(fromIndex, index1);
			} else
				newFieldName = fieldName.substring(fromIndex);
		}

		return newFieldName;
	}
	
	private ArrayList<ICompletionProposal> createProposals(ArrayList<String> proposals, int type)
	{
		if (proposals == null)
			return null;
		
		ArrayList<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
		for (int i = 0; i < proposals.size(); i++)
		{
			result.add( new VGLCompletionProposal(proposals.get(i), type));
		}
		
		return result;
	}
}
