package org.eclipse.pde.greendev.actions;

import org.eclipse.pde.greendev.GreenDev;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandService;

public class GreenDevStartup implements IStartup{

	@Override
	public void earlyStartup() {
		if (!GreenDev.getDefault().isEnabled()) {
			return;
		}
		
		GreenDev.getDefault().setEnabled(false);
		
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
				// TODO: Need to implement EnableGreenDev first
				// ICommand 
			}
		});
		
	}
}
