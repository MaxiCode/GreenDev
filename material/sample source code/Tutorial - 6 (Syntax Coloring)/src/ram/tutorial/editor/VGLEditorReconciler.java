package ram.tutorial.editor;

import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;

/**
 * 
 * @author Ram Kulkarni (rakulkar@adobe.com)
 *
 */
public class VGLEditorReconciler extends MonoReconciler {

	public VGLEditorReconciler(IReconcilingStrategy strategy,
			boolean isIncremental) {
		super(strategy, isIncremental);
	}
}
