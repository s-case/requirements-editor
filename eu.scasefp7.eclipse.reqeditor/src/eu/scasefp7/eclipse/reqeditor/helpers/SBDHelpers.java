package eu.scasefp7.eclipse.reqeditor.helpers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.scasefp7.eclipse.reqeditor.Activator;

/**
 * Class including helper functions for extracting requirements from sbd files.
 * 
 * @author themis
 */
public class SBDHelpers {

	/**
	 * Returns a list of requirements given an Eclipse resource.
	 * 
	 * @param file the Eclipse resource.
	 * @return a list of requirements.
	 */
	public static ArrayList<String> getRequirements(IFile file) {
		ArrayList<String> requirements = null;
		try {
			requirements = getRequirements(file.getContents());
		} catch (CoreException e) {
			Activator.log("Error reading requirements of sbd file", e);
		}
		return requirements;
	}

	/**
	 * Returns a list of requirements given the string of a file.
	 * 
	 * @param string the string containing requirements and annotations.
	 * @return a list of requirements.
	 */
	public static ArrayList<String> getRequirements(String string) {
		InputStream stringstream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
		ArrayList<String> requirements = getRequirements(stringstream);
		return requirements;
	}

	/**
	 * Returns a list of requirements given a file.
	 * 
	 * @param file the file of the file system.
	 * @return a list of requirements.
	 */
	public static ArrayList<String> getRequirements(File file) {
		ArrayList<String> requirements = null;
		try {
			InputStream filestream = new FileInputStream(file);
			requirements = getRequirements(filestream);
		} catch (FileNotFoundException e) {
			Activator.log("Error reading requirements of sbd file", e);
		}
		return requirements;
	}

	/**
	 * Returns a list of requirements given a {@link InputStream} object.
	 * 
	 * @param file the {@link InputStream} object.
	 * @return a list of requirements.
	 */
	public static ArrayList<String> getRequirements(InputStream stream) {
		ArrayList<String> requirements = new ArrayList<String>();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document dom = db.parse(stream);
			Element doc = dom.getDocumentElement();
			doc.normalize();

			Node root = doc.getElementsByTagName("auth.storyboards:StoryboardDiagram").item(0);
			NodeList nodes = root.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeName().equals("storyboardactions")) {
					requirements.add(node.getAttributes().getNamedItem("name").getTextContent());
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			Activator.log("Error reading requirements of sbd file", e);
		}

		return requirements;
	}

}
