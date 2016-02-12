package eu.scasefp7.eclipse.reqeditor.handlers;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.scasefp7.eclipse.reqeditor.Activator;

/**
 * A command handler for deleting all the annotations of a uml file.
 * 
 * @author themis
 */
public class ClearAllAnnotationsUMLHandler extends EditorAwareHandler {

	/**
	 * This function is called when the user selects the menu item. It reads the selected resource(s) and deletes all
	 * the annotations.
	 * 
	 * @param event the event containing the information about which file was selected.
	 * @return the result of the execution which must be {@code null}.
	 */
	@SuppressWarnings("unchecked")
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			List<Object> selectionList = structuredSelection.toList();
			// Iterate over the selected files
			for (Object object : selectionList) {
				IFile file = (IFile) Platform.getAdapterManager().getAdapter(object, IFile.class);
				if (file == null) {
					if (object instanceof IAdaptable) {
						file = (IFile) ((IAdaptable) object).getAdapter(IFile.class);
					}
				}
				if (file != null) {
					// Clear the new annotations and update any open editors
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db;
					try {
						db = dbf.newDocumentBuilder();
						Document dom = db.parse(file.getContents());
						Element doc = dom.getDocumentElement();
						doc.normalize();

						Node root = doc.getFirstChild().getNextSibling();
						NodeList nodes = root.getChildNodes();
						for (int i = 0; i < nodes.getLength(); i++) {
							Node node = nodes.item(i);
							if (node.getNodeName().equals("node")
									&& (node.getAttributes().getNamedItem("xmi:type").getTextContent()
											.equals("uml:OpaqueAction") || node.getAttributes()
											.getNamedItem("xmi:type").getTextContent().equals("uml:UseCaseNode"))) {
								if (node.getAttributes().getNamedItem("annotations") != null)
									node.getAttributes().getNamedItem("annotations").setTextContent("");
							}
						}
						TransformerFactory tf = TransformerFactory.newInstance();
						Transformer transformer = tf.newTransformer();
						transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
						StringWriter writer = new StringWriter();
						transformer.transform(new DOMSource(doc), new StreamResult(writer));
						String ntext = writer.getBuffer().toString();
						writeStringToFile(ntext, file);
						final IFile ffile = file;
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								updateEditor(ffile);
							}
						});
					} catch (TransformerException | ParserConfigurationException | SAXException | IOException
							| CoreException e) {
						Activator.log("Error reading or removing annotations of UML diagram", e);
					}
				}
			}
		}
		return null;
	}
}
