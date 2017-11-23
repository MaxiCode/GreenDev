package ram.tutorial.editor;

import java.util.ArrayList;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import ram.tutorial.editor.outline.VGLOutlinePage;
import ram.tutorial.editor.parser.ASTStart;
import ram.tutorial.editor.util.EditorUtil;

/**
 * 
 * @author Ram Kulkarni (rakulkar@adobe.com)
 *
 */
public class VGLEditor extends TextEditor {

	VGLSourceViewConfiguration sourceViewerConfiguration  = null;
	
	VGLOutlinePage outlinePage = null;
	
	ASTStart startNode = null;
	
	public VGLEditor() {
		super();
		sourceViewerConfiguration = new VGLSourceViewConfiguration(this);
	}

	public void createPartControl(Composite parent)
	{
		this.setSourceViewerConfiguration(sourceViewerConfiguration);
		super.createPartControl(parent);
		
		ISourceViewer sourceViewer = getSourceViewer();
		sourceViewer.getTextWidget().addLineStyleListener(new LineStyleListener (){

			public void lineGetStyle(LineStyleEvent event) {
				getLineStyle(event);
			}
		});
	}

	protected void getLineStyle(LineStyleEvent event) {
		
		if (startNode == null)
			return;
		
		int lineNum = -1;
		IDocument doc = getDocument();
		
		try
		{
			lineNum = doc.getLineOfOffset(event.lineOffset) + 1;

			if (lineNum < 1)
				return;
			
			ArrayList<StyleRange> styles = new ArrayList<StyleRange>();
			
			VGLColorizer.getInstance().colorizeLine(lineNum, event.lineOffset, startNode, styles);
			
			if (styles.size() > 0)
			{
				StyleRange[] styleArray = new StyleRange[styles.size()];
				
				for (int i = 0; i < styles.size(); i++)
					styleArray[i] = styles.get(i);
				
				event.styles = styleArray;
			}
			
		} catch (Exception e) {}
		
	}

	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			if (outlinePage == null) {
				outlinePage= new VGLOutlinePage(getDocumentProvider(), this);
				if (getEditorInput() != null)
					outlinePage.setInput(this);
			}
			return outlinePage;
		}
		return super.getAdapter(required);
	}
	
	public IDocument getDocument()
	{
		return getSourceViewer().getDocument();
	}
	
	public ASTStart getStartNode()
	{
		if (startNode == null)
		{
			parseDocument();
			//refresh view
			redrawViewer();
		}
		return startNode;
	}
	
	public ASTStart parseDocument()
	{
		startNode = EditorUtil.parseDocument(getDocument());
		return startNode;
	}
	
	public void redrawViewer()
	{
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				//refresh view
				if ( getSourceViewer() != null)
					 getSourceViewer().getTextWidget().redraw();
			}
		});
		
	}
	
}
