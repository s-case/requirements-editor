package reqeditor.helpers;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An empty implementation of {@link IProgressMonitor} used for fast operations, such as saving files.
 * 
 * @author themis
 */
public class MyProgressMonitor implements IProgressMonitor {

	public void worked(int work) {
	}

	public void subTask(String name) {
	}

	public void setTaskName(String name) {
	}

	public void setCanceled(boolean value) {
	}

	public boolean isCanceled() {
		return false;
	}

	public void internalWorked(double work) {
	}

	public void done() {
	}

	public void beginTask(String name, int totalWork) {
	}

}
