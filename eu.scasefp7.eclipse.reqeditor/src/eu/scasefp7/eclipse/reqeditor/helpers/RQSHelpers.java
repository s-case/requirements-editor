package eu.scasefp7.eclipse.reqeditor.helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * Class including helper functions for extracting requirements and annotations from rqs files.
 * 
 * @author themis
 */
public class RQSHelpers {

	/**
	 * Reads an Eclipse resource and extracts the requirements and the annotations, both as strings.
	 * 
	 * @param file the Eclipse resource.
	 * @return a string array, containing the requirements in the first position and the annotations in the second.
	 */
	public static String[] getRequirementsAndAnnotationsStrings(IFile file) {
		String[] txtandann = new String[2];

		Scanner scanner;
		String requirementsString = "";
		String annotationsString = "";
		try {
			scanner = new Scanner(file.getContents(), "UTF-8");
			ArrayList<String> requirements = new ArrayList<String>();
			ArrayList<String> annotations = new ArrayList<String>();
			boolean parseRequirements = false;
			boolean parseAnnotations = false;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (parseRequirements) {
					if (line.equals("------------"))
						parseRequirements = false;
					else
						requirements.add(line);
				}
				if (parseAnnotations) {
					if (line.equals("------------"))
						parseAnnotations = false;
					else {
						annotations.add(line);
					}
				}
				if (line.equals("REQUIREMENTS")) {
					parseRequirements = true;
					line = scanner.nextLine();
				}
				if (line.equals("ANNOTATIONS")) {
					parseAnnotations = true;
					line = scanner.nextLine();
				}
			}
			scanner.close();
			for (String requirement : requirements) {
				requirementsString += requirement + "\n";
			}
			for (String annotation : annotations) {
				annotationsString += annotation + "\n";
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		txtandann[0] = requirementsString;
		txtandann[1] = annotationsString;
		return txtandann;
	}

	/**
	 * Returns a list of requirements given an Eclipse resource.
	 * 
	 * @param file the Eclipse resource.
	 * @return a list of requirements.
	 */
	public static ArrayList<String> getRequirements(IFile file) {
		ArrayList<String> requirements = null;
		try {
			Scanner scanner = new Scanner(file.getContents(), "UTF-8");
			requirements = getRequirements(scanner);
			scanner.close();
		} catch (CoreException e) {
			e.printStackTrace();
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
		Scanner scanner = new Scanner(string);
		ArrayList<String> requirements = getRequirements(scanner);
		scanner.close();
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
			Scanner scanner = new Scanner(file, "UTF-8");
			requirements = getRequirements(scanner);
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return requirements;
	}

	/**
	 * Returns a list of requirements given a {@link Scanner} object.
	 * 
	 * @param file the {@link Scanner} object.
	 * @return a list of requirements.
	 */
	public static ArrayList<String> getRequirements(Scanner scanner) {
		ArrayList<String> requirements = new ArrayList<String>();
		boolean parseRequirements = false;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (parseRequirements) {
				if (line.equals("------------"))
					parseRequirements = false;
				else
					requirements.add(line);
			}
			if (line.equals("REQUIREMENTS")) {
				parseRequirements = true;
				line = scanner.nextLine();
			}
		}
		return requirements;
	}

	/**
	 * Returns a list of annotations given an Eclipse resource.
	 * 
	 * @param file the Eclipse resource.
	 * @return a list of annotations.
	 */
	public static ArrayList<String> getAnnotations(IFile file) {
		ArrayList<String> annotations = null;
		try {
			Scanner scanner = new Scanner(file.getContents(), "UTF-8");
			annotations = getAnnotations(scanner);
			scanner.close();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return annotations;
	}

	/**
	 * Returns a list of annotations given the string of a file.
	 * 
	 * @param string the string containing requirements and annotations.
	 * @return a list of annotations.
	 */
	public static ArrayList<String> getAnnotations(String string) {
		Scanner scanner = new Scanner(string);
		ArrayList<String> annotations = getAnnotations(scanner);
		scanner.close();
		return annotations;
	}

	/**
	 * Returns a list of annotations given a file.
	 * 
	 * @param file the file of the file system.
	 * @return a list of annotations.
	 */
	public static ArrayList<String> getAnnotations(File file) {
		ArrayList<String> annotations = null;
		try {
			Scanner scanner = new Scanner(file, "UTF-8");
			annotations = getAnnotations(scanner);
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return annotations;
	}

	/**
	 * Returns a list of requirements given a {@link Scanner} object.
	 * 
	 * @param file the {@link Scanner} object.
	 * @return a list of requirements.
	 */
	public static ArrayList<String> getAnnotations(Scanner scanner) {
		ArrayList<String> annotations = new ArrayList<String>();
		boolean parseAnnotations = false;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (parseAnnotations) {
				if (line.equals("------------"))
					parseAnnotations = false;
				else
					annotations.add(line);
			}
			if (line.equals("ANNOTATIONS")) {
				parseAnnotations = true;
				line = scanner.nextLine();
			}
		}
		return annotations;
	}

}
