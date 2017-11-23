package ram.tutorial.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import ram.tutorial.editor.outline.VGLOutlinePage;

/**
 * 
 * @author Ram Kulkarni (rakulkar@adobe.com)
 *
 */
public class VGLReconcilingStrategy implements IReconcilingStrategy {

	IDocument doc = null;
	VGLEditor editor = null;
	ISourceViewer sourceViewer = null;
	
	public void reconcile(IRegion partition) {
		//we support only one partition
		
		if (editor == null)
			return;
		
		//parse the document 
		editor.parseDocument();
		
		VGLOutlinePage outlinePage = (VGLOutlinePage) editor.getAdapter(IContentOutlinePage.class);
		
		outlinePage.refreshTree();
		
		editor.redrawViewer();
	}

	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {

	}

	public void setDocument(IDocument document) {
		doc = document;
	}

	public void setEditor(VGLEditor editor)
	{
		this.editor = editor;
	}
	
	public void setSourceViewer (ISourceViewer viewer)
	{
		this.sourceViewer = viewer;
	}
}
