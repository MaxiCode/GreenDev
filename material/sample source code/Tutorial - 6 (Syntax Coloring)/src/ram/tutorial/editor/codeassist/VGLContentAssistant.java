package ram.tutorial.editor.codeassist;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;

/**
 * 
 * @author Ram Kulkarni (rakulkar@adobe.com)
 *
 */
public class VGLContentAssistant extends ContentAssistant {

	public VGLContentAssistant()
	{
		setContentAssistProcessor (new VGLCompletionProcessor(), 
				IDocument.DEFAULT_CONTENT_TYPE);
		
		enableAutoActivation(true);
		
		setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		
	}
}
