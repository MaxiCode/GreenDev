package ram.tutorial.editor.vgl.plugin;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * 
 * @author Ram Kulkarni (rakulkar@adobe.com)
 * 
 * The activator class controls the plug-in life cycle
 */
public class VGLPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ram.tutorial.editor.VGLPlugin";

	// The shared instance
	private static VGLPlugin plugin;
	
	/**
	 * The constructor
	 */
	public VGLPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static VGLPlugin getDefault() {
		return plugin;
	}

}
