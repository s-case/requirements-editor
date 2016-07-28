package eu.scasefp7.eclipse.reqeditor.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.ui.editors.text.TextEditor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.scasefp7.eclipse.reqeditor.Activator;
import eu.scasefp7.eclipse.reqeditor.helpers.MyProgressMonitor;

/**
 * This class reads a uml file and extracts all requirements and annotations. Additionally, it recreates the file when
 * any changes are made.
 * 
 * @author themis
 */
public class UMLPhrasesReader extends RequirementsReader {

	/**
	 * The main document element of the uml file.
	 */
	private Element doc;

	/**
	 * Initializes this reader.
	 */
	public UMLPhrasesReader() {
		super();
	}

	/**
	 * Initializes this reader providing the raw editor object.
	 * 
	 * @param editor the editor containing the raw uml file.
	 */
	public UMLPhrasesReader(TextEditor editor) {
		super(editor);
	}

	/**
	 * Parses data from an {@link InputStream} and extracts the requirements and the annotations.
	 * 
	 * @param scanner the {@link InputStream} to parse data from.
	 */
	public void parseData(InputStream stream) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document dom = db.parse(stream);
			doc = dom.getDocumentElement();
			doc.normalize();

			Node root;
			Node secondChild = doc.getFirstChild().getNextSibling();
			if (secondChild.getNodeName().equals("packagedElement")
					&& secondChild.getAttributes().getNamedItem("xmi:type").getTextContent().equals("uml:Activity")) {
				root = secondChild;
			} else
				root = doc;
			NodeList nodes = root.getChildNodes();
			int k = 0;
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if ((node.getNodeName().equals("packagedElement") && node.getAttributes().getNamedItem("xmi:type")
						.getTextContent().equals("uml:UseCase"))
						|| (node.getNodeName().equals("node") && node.getAttributes().getNamedItem("xmi:type")
								.getTextContent().equals("uml:OpaqueAction"))) {
					k++;
					requirements.add(new Requirement(k, node.getAttributes().getNamedItem("name").getTextContent()));
					if (node.getAttributes().getNamedItem("annotations") != null
							&& !node.getAttributes().getNamedItem("annotations").getNodeValue().equals("")) {
						String[] lineAnnotations = node.getAttributes().getNamedItem("annotations").getTextContent()
								.split("\\\\n");
						for (String line : lineAnnotations) {
							parseAnnotation(line, true);
						}

					}
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			Activator.log("Error reading requirements and annotations from stream", e);
		}
	}

	/**
	 * Refreshes the requirements and the annotations and saves the editor. This function must be called whenever a
	 * requirement or an annotation is modified.
	 */
	public void refresh() {
		Node root;
		Node secondChild = doc.getFirstChild().getNextSibling();
		if (secondChild.getNodeName().equals("packagedElement")
				&& secondChild.getAttributes().getNamedItem("xmi:type").getTextContent().equals("uml:Activity")) {
			root = secondChild;
		} else
			root = doc;
		NodeList nodes = root.getChildNodes();
		int k = 0;
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if ((node.getNodeName().equals("packagedElement") && node.getAttributes().getNamedItem("xmi:type")
					.getTextContent().equals("uml:UseCase"))
					|| (node.getNodeName().equals("node") && node.getAttributes().getNamedItem("xmi:type")
							.getTextContent().equals("uml:OpaqueAction"))) {
				k++;
				String annotationsText = "";
				for (TAnnotation tannotation : getTAnnotationsByRequirementId(k)) {
					annotationsText += tannotation.toString(true) + "\\n";
				}
				for (RAnnotation rannotation : getRAnnotationsByRequirementId(k)) {
					annotationsText += rannotation.toString(true) + "\\n";
				}
				if (!annotationsText.equals("")) {
					annotationsText = annotationsText.substring(0, annotationsText.length() - 2);
					if (node.getAttributes().getNamedItem("annotations") == null)
						node.getAttributes().setNamedItem(node.getOwnerDocument().createAttribute("annotations"));
					node.getAttributes().getNamedItem("annotations").setTextContent(annotationsText);
				}
			}
		}
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			String output = writer.getBuffer().toString();
			if (editor != null) {
				editor.getDocumentProvider().getDocument(editor.getEditorInput()).set(output);
				editor.doSave(new MyProgressMonitor());
			}
		} catch (TransformerException e) {
			Activator.log("Error updating the annotations of a UML diagram file", e);
		}
	}

}
