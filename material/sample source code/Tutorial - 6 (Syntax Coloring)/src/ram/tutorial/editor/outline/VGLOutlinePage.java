package ram.tutorial.editor.outline;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import ram.tutorial.editor.VGLEditor;
import ram.tutorial.editor.parser.Token;

public class VGLOutlinePage extends ContentOutlinePage {

	IDocumentProvider docProvider = null;
	VGLEditor editor = null;
	Object editorInput = null;
	TreeViewer viewer = null;
	
	public VGLOutlinePage(IDocumentProvider documentProvider, VGLEditor editor) {
		this.docProvider = documentProvider;
		this.editor = editor;
	}

	public void setInput(Object editorInput) {
		this.editorInput = editorInput;
	}

	public void createControl(Composite parent)
	{
		super.createControl(parent);
	
		viewer= getTreeViewer();
		viewer.setContentProvider(new VGLOutlineContentProvider());
		viewer.setLabelProvider(new LabelProvider());
		viewer.addSelectionChangedListener(this);
	
		if (editorInput != null)
			viewer.setInput(editorInput);
	}

    public void selectionChanged(SelectionChangedEvent event)
    {
    	Object selObj = ((TreeSelection)event.getSelection()).getFirstElement();
    	
    	if (selObj instanceof OutlineNode == false)
    		return;
    	
    	OutlineNode outlineNode = (OutlineNode) selObj;
    	
    	if (outlineNode.node == null)
    		return;
    		
    	IDocument doc = editor.getDocument();
    	
    	Token tk = outlineNode.node.jjtGetFirstToken();
    	
    	try
    	{
    		int lineOffset = doc.getLineOffset(tk.beginLine-1);
    		int startOffset = lineOffset + tk.beginColumn - 1;
    		int endOffset = lineOffset + tk.endColumn + 1;
    		editor.getSelectionProvider().setSelection(new TextSelection(startOffset, endOffset - startOffset));
    	} catch (Exception e)
    	{
    		//ignore
    	}
    }
    
    public void refreshTree()
    {
    	Display.getDefault().asyncExec(new Runnable() {

			public void run() {
		    	viewer.refresh();
			}
    		
    	});
    }

}
