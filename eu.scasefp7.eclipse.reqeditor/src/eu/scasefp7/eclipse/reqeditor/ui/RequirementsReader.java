package eu.scasefp7.eclipse.reqeditor.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.eclipse.ui.editors.text.TextEditor;

import eu.scasefp7.eclipse.reqeditor.helpers.MyProgressMonitor;
import eu.scasefp7.eclipse.reqeditor.ui.annotationseditor.IdHashMap;

/**
 * This class reads an rqs file and extracts all requirements and annotations. Additionally, it recreates the file when
 * any changes are made.
 * 
 * @author themis
 */
public class RequirementsReader {

	/**
	 * The indent between sentences.
	 */
	public final int indent = 4;

	/**
	 * The editor containing the raw rws file.
	 */
	private TextEditor editor;

	/**
	 * An list of the requirements.
	 */
	private ArrayList<Requirement> requirements;

	/**
	 * A list of the annotations. It is used to keep the order of the annotations.
	 */
	private ArrayList<Annotation> annotations;

	/**
	 * A map containing all T-type annotations.
	 */
	private IdHashMap<TAnnotation> tannotations;

	/**
	 * A map containing all R-type annotations.
	 */
	private IdHashMap<RAnnotation> rannotations;

	/**
	 * Initializes this reader.
	 */
	public RequirementsReader() {
		requirements = new ArrayList<Requirement>();
		annotations = new ArrayList<Annotation>();
		rannotations = new IdHashMap<RAnnotation>("R");
		tannotations = new IdHashMap<TAnnotation>("T");
	}

	/**
	 * Initializes this reader providing the raw editor object.
	 * 
	 * @param editor the editor containing the raw rws file.
	 */
	public RequirementsReader(TextEditor editor) {
		this.editor = editor;
		requirements = new ArrayList<Requirement>();
		annotations = new ArrayList<Annotation>();
		rannotations = new IdHashMap<RAnnotation>("R");
		tannotations = new IdHashMap<TAnnotation>("T");
	}

	/**
	 * Find the word of an annotation of which the limits and the requirement id are given.
	 * 
	 * @param annLimits the limits of the annotation.
	 * @param reqnum the requirement of the annotation.
	 * @return the word of the annotation.
	 */
	public String findWordOfAnnotation(Limits annLimits, int reqnum) {
		String reqString = requirements.get(reqnum - 1).text;
		if (annLimits.left >= 0 && annLimits.right < reqString.length() + 1)
			return reqString.substring(annLimits.left, annLimits.right);
		else
			return null;
	}

	/**
	 * Find which requirement contains the R-type annotation of which the T-type annotations are given.
	 * 
	 * @param Arg1 the id of the first T-type annotation.
	 * @param Arg2 the id of the second T-type annotation.
	 * @return the id of the requirement that contains the annotation.
	 */
	public int findRequirementOfAnnotation(String Arg1, String Arg2) {
		if (tannotations.containsKey(Arg1) && tannotations.containsKey(Arg2))
			return tannotations.get(Arg1).reqnum;
		else
			return 0;
	}

	/**
	 * Returns the id of the requirement that a selected text belongs to.
	 * 
	 * @param leftLineOfSelection the starting line of the selection.
	 * @param rightLineOfSelection the ending line of the selection.
	 * @return the id of the requirement that a selected text belongs to.
	 */
	public int findRequirementOfSelection(int leftLineOfSelection, int rightLineOfSelection) {
		if (leftLineOfSelection == rightLineOfSelection && (leftLineOfSelection + 1) % (indent + 1) == 0)
			return (leftLineOfSelection + 1) / (indent + 1);
		else
			return -1;
	}

	/**
	 * Parses a file and extracts the requirements and the annotations.
	 * 
	 * @param path the path of the file to be parsed.
	 */
	public void parseFile(String path) {
		try {
			Scanner filescanner = new Scanner(new File(path), "UTF-8");
			parseData(filescanner);
			filescanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Parses a string and extracts the requirements and the annotations.
	 * 
	 * @param string the string to be parsed.
	 */
	public void parseString(String string) {
		Scanner stringscanner = new Scanner(string);
		parseData(stringscanner);
		stringscanner.close();
	}

	/**
	 * Parses the input of the {@code editor} and extracts the requirements and the annotations.
	 */
	public void parseFromEditor() {
		if (editor != null) {
			requirements = new ArrayList<Requirement>();
			annotations = new ArrayList<Annotation>();
			rannotations = new IdHashMap<RAnnotation>("R");
			tannotations = new IdHashMap<TAnnotation>("T");
			Scanner stringscanner = new Scanner(editor.getDocumentProvider().getDocument(editor.getEditorInput()).get());
			parseData(stringscanner);
			stringscanner.close();
		}
	}

	/**
	 * Parses data from a {@link Scanner} object and extracts the requirements and the annotations.
	 * 
	 * @param scanner the {@link Scanner} to parse data from.
	 */
	public void parseData(Scanner scanner) {
		boolean parseRequirements = false;
		boolean parseAnnotations = false;

		int i = 0;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			// Parse requirements
			if (parseRequirements) {
				if (line.equals("------------"))
					parseRequirements = false;
				else
					requirements.add(new Requirement((++i), line));
			}
			// Parse annotations
			if (parseAnnotations) {
				if (line.equals("------------"))
					parseAnnotations = false;
				else {
					List<String> anndata = Arrays.asList(line.split("\t"));
					String Id = anndata.get(0);
					String[] annotationdata = anndata.get(1).split(" ");
					String type = annotationdata[0];
					if (Id.contains(":T")) {
						// Read T-type annotation
						String word = anndata.get(2);
						Limits annlimits = new Limits(annotationdata[1], annotationdata[2]);
						int reqnum = Integer.parseInt(Id.split(":")[0]);
						TAnnotation annotation = new TAnnotation(reqnum, Id, type, word, annlimits);
						tannotations.put(Id, annotation);
						annotations.add(annotation);
					} else if (Id.contains(":R")) {
						// Read R-type annotation
						String Arg1 = annotationdata[1];
						String Arg2 = annotationdata[2];
						int reqnum = Integer.parseInt(Id.split(":")[0]);
						if (reqnum > 0) {
							RAnnotation annotation = new RAnnotation(reqnum, Id, type, Arg1, Arg2);
							rannotations.put(Id, annotation);
							annotations.add(annotation);
						}
					}
				}
			}
			if (line.equals("REQUIREMENTS")) {
				parseRequirements = true;
				line = scanner.nextLine();
				i = 0;
			}
			if (line.equals("ANNOTATIONS")) {
				parseAnnotations = true;
				line = scanner.nextLine();
			}
		}
	}

	/**
	 * Returns the functional requirements.
	 * 
	 * @return an {@link ArrayList} containing the requirements.
	 */
	public ArrayList<Requirement> getRequirements() {
		return requirements;
	}

	/**
	 * Returns the T-type annotations.
	 * 
	 * @return a {@link Collection} of T-type annotations.
	 */
	public Collection<TAnnotation> getTAnnotations() {
		return tannotations.values();
	}

	/**
	 * Returns the R-type annotations.
	 * 
	 * @return a {@link Collection} of R-type annotations.
	 */
	public Collection<RAnnotation> getRAnnotations() {
		return rannotations.values();
	}

	/**
	 * Returns the R-type annotations that are contained in the requirement of which the id is given.
	 * 
	 * @param requirementId the id of the requirement that the R-type annotations must belong to.
	 * @return an {@link ArrayList} of the R-type annotations that belong to the requirement.
	 */
	public ArrayList<RAnnotation> getRAnnotationsByRequirementId(int requirementId) {
		ArrayList<RAnnotation> annotations = new ArrayList<RAnnotation>();
		for (RAnnotation annotation : rannotations.values()) {
			if (annotation.reqnum == requirementId)
				annotations.add(annotation);
		}
		return annotations;
	}

	/**
	 * Returns the T-type annotations that are contained in the requirement of which the id is given.
	 * 
	 * @param requirementId the id of the requirement that the T-type annotations must belong to.
	 * @return an {@link ArrayList} of the T-type annotations that belong to the requirement.
	 */
	public ArrayList<TAnnotation> getTAnnotationsByRequirementId(int requirementId) {
		ArrayList<TAnnotation> annotations = new ArrayList<TAnnotation>();
		for (TAnnotation annotation : tannotations.values()) {
			if (annotation.reqnum == requirementId)
				annotations.add(annotation);
		}
		return annotations;
	}

	/**
	 * Returns a T-type annotation given its id.
	 * 
	 * @param Id the id of the annotation to be returned.
	 * @return the T-type annotation of which the id is given.
	 */
	public TAnnotation getTAnnotation(String Id) {
		return tannotations.get(Id);
	}

	/**
	 * Returns a R-type annotation given its id.
	 * 
	 * @param Id the id of the annotation to be returned.
	 * @return the R-type annotation of which the id is given.
	 */
	public RAnnotation getRAnnotation(String Id) {
		return rannotations.get(Id);
	}

	/**
	 * Swaps the position of two requirements given their ids. This function also updates all annotations of the two
	 * requirements to be concise with the swap.
	 * 
	 * @param requirementIdOne the id of the first requirement to be swapped.
	 * @param requirementIdTwo the id of the second requirement to be swapped.
	 */
	public void swapRequirements(int requirementIdOne, int requirementIdTwo) {
		// Swap the requirements
		Collections.swap(requirements, requirementIdOne - 1, requirementIdTwo - 1);
		// Change the ids of the requirements
		requirements.get(requirementIdOne - 1).id = requirementIdOne;
		requirements.get(requirementIdTwo - 1).id = requirementIdTwo;
		// Change the ids of the annotations
		Iterator<Annotation> anniter = annotations.iterator();
		while (anniter.hasNext()) {
			Annotation annotation = anniter.next();
			if (annotation instanceof TAnnotation) {
				TAnnotation tannotation = (TAnnotation) annotation;
				if (tannotation.reqnum == requirementIdOne) {
					tannotations.remove(tannotation.Id);
					tannotation.setRequirementId(requirementIdTwo);
					tannotations.put(tannotation.Id, (TAnnotation) tannotation);
				} else if (tannotation.reqnum == requirementIdTwo) {
					tannotations.remove(tannotation.Id);
					tannotation.setRequirementId(requirementIdOne);
					tannotations.put(tannotation.Id, (TAnnotation) tannotation);
				}
			} else if (annotation instanceof RAnnotation) {
				RAnnotation rannotation = (RAnnotation) annotation;
				if (rannotation.reqnum == requirementIdOne) {
					rannotations.remove(rannotation.Id);
					rannotation.setRequirementId(requirementIdTwo);
					rannotations.put(rannotation.Id, (RAnnotation) rannotation);
				} else if (rannotation.reqnum == requirementIdTwo) {
					rannotations.remove(rannotation.Id);
					rannotation.setRequirementId(requirementIdOne);
					rannotations.put(rannotation.Id, (RAnnotation) rannotation);
				}
			}
		}
	}

	/**
	 * Deletes a requirement given its id. This function also deletes all the annotations of the requirement and moves
	 * shifts all the requirements to cover the position of the deleted requirement.
	 * 
	 * @param requirementId the id of the requirement to be deleted.
	 */
	public void deleteRequirement(int requirementId) {
		// Move the requirement to the last position of the list
		for (int i = requirementId; i < requirements.size(); i++) {
			swapRequirements(i, i + 1);
		}
		// Delete the annotations of the last requirement
		Iterator<Annotation> anniter = annotations.iterator();
		while (anniter.hasNext()) {
			Annotation annotation = anniter.next();
			if (annotation.reqnum == requirements.size()) {
				if (annotation instanceof TAnnotation) {
					tannotations.remove(annotation.Id);
				} else if (annotation instanceof RAnnotation) {
					rannotations.remove(annotation.Id);
				}
				anniter.remove();
			}
		}
		// Delete the requirement
		requirements.remove(requirements.size() - 1);
	}

	/**
	 * Adds a new requirement given its id and its text.
	 * 
	 * @param requirementId the id of the newly added requirement.
	 * @param text the text of the newly added requirement.
	 */
	public void addRequirement(int requirementId, String text) {
		requirements.add(new Requirement(requirementId, text));
	}

	/**
	 * Modifies the text of a requirement. This function also deletes all the annotations of the requirement prior to
	 * modifying it.
	 * 
	 * @param requirementId the id of the requirement to be modified.
	 * @param text the new text of the requirement.
	 */
	public void modifyRequirement(int requirementId, String text) {
		// Delete the annotations of the requirement
		Iterator<Annotation> anniter = annotations.iterator();
		while (anniter.hasNext()) {
			Annotation annotation = anniter.next();
			if (annotation.reqnum == requirementId) {
				if (annotation instanceof TAnnotation) {
					tannotations.remove(annotation.Id);
				} else if (annotation instanceof RAnnotation) {
					rannotations.remove(annotation.Id);
				}
				anniter.remove();
			}
		}
		// Modify the text of the requirement
		requirements.get(requirementId - 1).text = text;
	}

	/**
	 * Creates and adds a new T-type annotation.
	 * 
	 * @param type the type of the newly added annotation.
	 * @param annLimits the ann limits of the newly added annotation.
	 * @param reqnum the requirement of the newly added annotation.
	 */
	public void addTAnnotation(String type, Limits annLimits, int reqnum) {
		String Id = tannotations.getNewId(reqnum);
		String word = findWordOfAnnotation(annLimits, reqnum);
		TAnnotation annotation = new TAnnotation(reqnum, Id, type, word, annLimits);
		tannotations.put(Id, annotation);
		annotations.add(annotation);
	}

	/**
	 * Creates and adds a new R-type annotation.
	 * 
	 * @param type the type of the newly added annotation.
	 * @param Arg1 the Id of the first T-type annotation of the newly added annotation.
	 * @param Arg2 the Id of the second T-type annotation of the newly added annotation.
	 */
	public void addRAnnotation(String type, String Arg1, String Arg2) {
		int reqnum = findRequirementOfAnnotation(Arg1, Arg2);
		String Id = rannotations.getNewId(reqnum);
		if (getTAnnotation(Arg1) != null && getTAnnotation(Arg2) != null){
			RAnnotation annotation = new RAnnotation(reqnum, Id, type, Arg1, Arg2);
			rannotations.put(Id, annotation);
			annotations.add(annotation);
		}
	}

	/**
	 * Removes an annotation.
	 * 
	 * @param annotation the annotation to be removed.
	 */
	public void removeAnnotation(Annotation annotation) {
		annotations.remove(annotation);
		if (annotation.Id.contains(":T"))
			tannotations.remove(annotation.Id);
		else
			rannotations.remove(annotation.Id);
	}

	/**
	 * Returns a string containing all the requirements.
	 * 
	 * @return a string containing all the requirements.
	 */
	public String getRequirementsString() {
		String res = "";
		for (Requirement requirement : requirements) {
			res += requirement + "\n";
		}
		return res;
	}

	/**
	 * Returns a string containing all the annotations.
	 * 
	 * @return a string containing all the annotations.
	 */
	public String getAnnotationsString() {
		String res = "";
		for (Annotation annotation : annotations) {
			res += annotation + "\n";
		}
		return res;
	}

	/**
	 * Sets a new requirements string and saves the editor.
	 * 
	 * @param reqsText the new requirements string to be added.
	 */
	public void setRequirementsStringAndRefresh(String reqsText) {
		String finalReqsText = "";
		Scanner stringscanner = new Scanner(reqsText);
		while (stringscanner.hasNextLine()) {
			String line = stringscanner.nextLine();
			if (line.length() > 0)
				finalReqsText += line + "\n";
		}
		stringscanner.close();
		String ntext = "REQUIREMENTS\n------------\n";
		ntext += finalReqsText;
		ntext += "------------\n\nANNOTATIONS\n------------\n";
		ntext += getAnnotationsString();
		ntext += "------------\n";
		if (editor != null) {
			editor.getDocumentProvider().getDocument(editor.getEditorInput()).set(ntext);
			editor.doSave(new MyProgressMonitor());
		}
	}

	/**
	 * Refreshes the requirements and the annotations and saves the editor. This function must be called whenever a
	 * requirement or an annotation is modified.
	 */
	public void refresh() {
		String ntext = "REQUIREMENTS\n------------\n";
		ntext += getRequirementsString();
		ntext += "------------\n\nANNOTATIONS\n------------\n";
		ntext += getAnnotationsString();
		ntext += "------------\n";
		if (editor != null) {
			editor.getDocumentProvider().getDocument(editor.getEditorInput()).set(ntext);
			editor.doSave(new MyProgressMonitor());
		}
	}

	/**
	 * Check if there are any annotations.
	 * 
	 * @return {@code true} if there are any annotations, and {@code false} otherwise.
	 */
	public boolean hasAnnotations() {
		return !annotations.isEmpty();
	}

	/**
	 * Checks whether an R-type annotation already exists.
	 * 
	 * @param type the type of the annotation that is checked.
	 * @param Arg1 the Id of the first T-type annotation of the annotation that is checked.
	 * @param Arg2 the Id of the second T-type annotation of the annotation that is checked.
	 * @return {@code true} if the annotation exists, and {@code false} otherwise.
	 */
	public boolean hasRAnnotation(String type, String Arg1, String Arg2) {
		return annotations.contains(new RAnnotation(0, "", type, Arg1, Arg2));
	}

}
