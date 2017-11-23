package ram.tutorial.editor;

import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import ram.tutorial.editor.codeassist.VGLContentAssistant;

/**
 * 
 * @author Ram Kulkarni (rakulkar@adobe.com)
 *
 */
public class VGLSourceViewConfiguration extends SourceViewerConfiguration {

	VGLContentAssistant contentAssistant = new VGLContentAssistant();
	VGLEditorReconciler reconciler = null;
	VGLReconcilingStrategy reconcilingStategy = null; 
	VGLEditor editor = null;
	
	public VGLSourceViewConfiguration(VGLEditor editor)
	{
		this.editor = editor;
	}
	
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		
		contentAssistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		return contentAssistant;
	}
	
	public IReconciler getReconciler (ISourceViewer sourceViewer)
	{
		if (reconcilingStategy == null)
			reconcilingStategy = new VGLReconcilingStrategy();
		
		if (reconciler == null)
			reconciler = new VGLEditorReconciler(reconcilingStategy, false);
		
		reconcilingStategy.setEditor(editor);
		reconcilingStategy.setSourceViewer(sourceViewer);
		
		return reconciler;
	}
}
