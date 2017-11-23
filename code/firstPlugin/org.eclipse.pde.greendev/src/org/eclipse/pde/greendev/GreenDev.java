package org.eclipse.pde.greendev;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class GreenDev extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.pde.greendev";
	public static final String PREF_ENABLED = "enabled";
	public static final String PREF_DEFAULT_THEME = "default_theme";
	
	private static GreenDev plugin;
	
	 
	/*
	 * TODO: Implement GreenDevProviderRegistry
	private GreenDevProviderRegistry registry;
	*/
	
	public GreenDev() {
	}
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}
	
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
	public static GreenDev getDefault() {
		return plugin;
	}
	
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	/*
	 * TODO: Implement GreenDevProviderRegistry
	public GreenDevProviderRegistry getProviderRegistry(){
		return registry == null ? (registry = new GreenDevProviderRegistry()) : registry;
	}
	*/
	
	/** Check if GreenDev is enabled */
	public boolean isEnabled() {
		return getPreferenceStore().getBoolean(PREF_ENABLED);
	}
	
	public void setEnabled(boolean flag) {
		getPreferenceStore().setValue(PREF_ENABLED, flag);
	}
	
	
	// TODO: need to be checked: ActionsetID, EnableGreenDevActionId
	public static void toggleToolbarItem(IWorkbenchWindow window, boolean enabled) {
		if (window instanceof ApplicationWindow) {
			CoolBarManager coolBarManager = ((ApplicationWindow) window).getCoolBarManager();
			if (coolBarManager != null) {
				IContributionItem item = coolBarManager.find("org.eclipse.pde.greendev.ActionsetID");
				if (item instanceof ToolBarContributionItem) {
					IToolBarManager tbMgr = ((ToolBarContributionItem) item).getToolBarManager();
					if (tbMgr != null) {
						IContributionItem item2 = tbMgr.find("org.eclipse.pde.greendev.EnableGreenDevActionId");
						if (item2 instanceof ActionContributionItem) {
							((ActionContributionItem) item2).getAction().setChecked(enabled);
						}
					}
				}
			}
		}
	}
	
	public static void toggleToolBarItemInAllWindows(boolean enabled) {
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			toggleToolbarItem(window, enabled);
		}
	}
	
	public static void logError(Object source, String msg, Throwable error) {
		String src = "";
		if (source instanceof Class<?>) {
			src = ((Class<?>) source).getName();
		} else {
			src = source.getClass().getName();
		}
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, 1, "[" + src + "] " + msg, error));
	}
}
