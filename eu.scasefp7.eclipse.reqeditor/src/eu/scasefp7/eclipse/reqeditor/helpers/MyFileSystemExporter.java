package eu.scasefp7.eclipse.reqeditor.helpers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.FileSystemExporter;

/**
 * Extends the {@link FileSystemExporter} class in order to write two files (a txt and an ann one) in the selected
 * directory.
 * 
 * @author themis
 */
@SuppressWarnings("restriction")
public class MyFileSystemExporter extends FileSystemExporter {

	/**
	 * The buffer size for writing to disk.
	 */
	private static final int DEFAULT_BUFFER_SIZE = 16 * 1024;

	/**
	 * Boolean denoting whether the txt must be exported.
	 */
	private boolean exportTxt;

	/**
	 * Boolean denoting whether the ann must be exported.
	 */
	private boolean exportAnn;

	/**
	 * Initializes this object and determines which type of files must be exported (both can be exported too).
	 * 
	 * @param exportTxt boolean denoting whether the txt must be exported.
	 * @param exportAnn boolean denoting whether the ann must be exported.
	 */
	public MyFileSystemExporter(boolean exportTxt, boolean exportAnn) {
		this.exportTxt = exportTxt;
		this.exportAnn = exportAnn;
	}

	/**
	 * Writes a string to a new file system resource, given its path.
	 * 
	 * @param string the string to be written to file.
	 * @param destinationPath the path to write the new file to.
	 * @throws IOException
	 * @throws CoreException
	 */
	protected void writeString(String string, IPath destinationPath) throws IOException, CoreException {
		OutputStream output = null;
		InputStream contentStream = null;

		try {
			contentStream = new BufferedInputStream(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
			output = new BufferedOutputStream(new FileOutputStream(destinationPath.toOSString()));
			// for large files, need to make sure the chunk size can be handled by the VM
			int available = contentStream.available();
			available = available <= 0 ? DEFAULT_BUFFER_SIZE : available;
			int chunkSize = Math.min(DEFAULT_BUFFER_SIZE, available);
			byte[] readBuffer = new byte[chunkSize];
			int n = contentStream.read(readBuffer);

			while (n > 0) {
				// only write the number of bytes read
				output.write(readBuffer, 0, n);
				n = contentStream.read(readBuffer);
			}
		} finally {
			if (contentStream != null) {
				// wrap in a try-catch to ensure attempt to close output stream
				try {
					contentStream.close();
				} catch (IOException e) {
					IDEWorkbenchPlugin.log("Error closing input stream for file: " + destinationPath, e); //$NON-NLS-1$
				}
			}
			if (output != null) {
				// propogate this error to the user
				output.close();
			}
		}
	}

	/**
	 * Writes the passed file resource to the specified destination on the local file system.
	 * 
	 * @param file the file resource.
	 * @param destinationPath the destination on the local file system.
	 */
	protected void writeFile(IFile file, IPath destinationPath) throws IOException, CoreException {
		if (!exportTxt && !exportAnn)
			super.writeFile(file, destinationPath);
		else {
			String RQS = "";
			try {
				Scanner scanner = new Scanner(file.getContents(), "UTF-8");
				while (scanner.hasNextLine()) {
					RQS += scanner.nextLine() + "\n";
				}
				scanner.close();
			} catch (CoreException e) {
				e.printStackTrace();
			}
			String[] txtandann = RQStoANNHelpers.transformRQStoTXTandANN(RQS);
			if (exportTxt)
				writeString(txtandann[0], destinationPath.removeFileExtension().addFileExtension("txt"));
			if (exportAnn)
				writeString(txtandann[1], destinationPath.removeFileExtension().addFileExtension("ann"));
		}
	}
}
