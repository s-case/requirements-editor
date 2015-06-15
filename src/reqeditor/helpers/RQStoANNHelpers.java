package reqeditor.helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Class including helper functions for transforming rqs files to txt and ann files and vice versa.
 * 
 * @author themis
 */
public class RQStoANNHelpers {

	/**
	 * Transforms an rqs string to two strings for requirements (txt) and annotations (ann) to support the ann format.
	 * 
	 * @param RQS the rqs string to be transformed.
	 * @return an array containing the requirements in the first position and the annotations (ann) in the second.
	 */
	public static String[] transformRQStoTXTandANN(String RQS) {
		String[] txtandann = new String[2];
		ArrayList<String> requirements = RQSHelpers.getRequirements(RQS);
		ArrayList<String> annotations = RQSHelpers.getAnnotations(RQS);
		HashMap<Integer, Integer> startChars = new HashMap<Integer, Integer>();
		int i = 0;
		int c = 0;
		String txtRequirements = "";
		for (String requirement : requirements) {
			i++;
			startChars.put(i, c);
			c += requirement.length() + 1;
			txtRequirements += requirement + "\n";
		}
		int currentTid = 0;
		int currentRid = 0;
		HashMap<String, String> idmap = new HashMap<String, String>();
		for (String annotation : annotations) {
			String Id = annotation.split("\t")[0];
			String type = Id.split(":")[1].substring(0, 1);
			if (type.equals("T"))
				idmap.put(Id, "T" + (++currentTid));
			else if (type.equals("R"))
				idmap.put(Id, "R" + (++currentRid));
		}
		String annAnnotations = "";
		for (String annotation : annotations) {
			String Id = annotation.split("\t")[0];
			String type = Id.split(":")[1].substring(0, 1);
			int reqId = Integer.parseInt(Id.split(":")[0]);
			if (type.equals("T")) {
				String anntype = annotation.split("\t")[1];
				String annword = annotation.split("\t")[2];
				int leftlim = startChars.get(reqId) + Integer.parseInt(anntype.split(" ")[1]);
				int rightlim = startChars.get(reqId) + Integer.parseInt(anntype.split(" ")[2]);
				anntype = anntype.split(" ")[0] + " " + leftlim + " " + rightlim;
				annAnnotations += idmap.get(Id) + "\t" + anntype + "\t" + annword + "\n";
			} else if (type.equals("R")) {
				String anntype = annotation.split("\t")[1];
				anntype = anntype.split(" ")[0] + " Arg1:" + idmap.get(anntype.split(" ")[1]) + " Arg2:"
						+ idmap.get(anntype.split(" ")[2]);
				annAnnotations += idmap.get(Id) + "\t" + anntype + "\n";
			}
		}
		txtandann[0] = txtRequirements;
		txtandann[1] = annAnnotations;
		return txtandann;
	}

	/**
	 * Transforms a txt and an ann string to a string complying to the rqs format.
	 * 
	 * @param TXT the txt string to be transformed.
	 * @param ANN the txt string to be transformed.
	 * @return a string in rqs format.
	 */
	public static String transformTXTandANNtoRQS(String TXT, String ANN) {
		ArrayList<String> requirements = new ArrayList<String>();
		Scanner txtScanner = new Scanner(TXT);
		while (txtScanner.hasNextLine()) {
			requirements.add(txtScanner.nextLine());
		}
		txtScanner.close();

		ArrayList<String> annotations = new ArrayList<String>();
		Scanner annScanner = new Scanner(ANN);
		while (annScanner.hasNextLine()) {
			annotations.add(annScanner.nextLine());
		}
		annScanner.close();

		HashMap<Integer, Integer> startChars = new HashMap<Integer, Integer>();
		int i = 0;
		int c = 0;
		String txtRequirements = "";
		for (String requirement : requirements) {
			i++;
			startChars.put(i, c);
			c += requirement.length() + 1;
			txtRequirements += requirement + "\n";
		}

		HashMap<String, String> idmap = new HashMap<String, String>();
		int[] currentTid = new int[requirements.size()];
		Arrays.fill(currentTid, 0);
		int[] currentRid = new int[requirements.size()];
		Arrays.fill(currentRid, 0);
		for (String annotation : annotations) {
			String Id = annotation.split("\t")[0];
			String type = Id.substring(0, 1);
			if (type.equals("T")) {
				int leftLim = Integer.parseInt(annotation.split("\t")[1].split(" ")[1]);
				for (i = 1; i < requirements.size() + 1; i++) {
					if (leftLim < startChars.get(i))
						break;
				}
				int reqId = i - 1;
				idmap.put(Id, reqId + ":T" + (++currentTid[reqId - 1]));
			}
		}
		for (String annotation : annotations) {
			String Id = annotation.split("\t")[0];
			String type = Id.substring(0, 1);
			if (type.equals("R")) {
				String leftTId = annotation.split("\t")[1].split(" ")[1].split(":")[1];
				int reqId = Integer.parseInt(idmap.get(leftTId).split(":")[0]);
				idmap.put(Id, reqId + ":R" + (++currentRid[reqId - 1]));
			}
		}

		String annAnnotations = "";
		for (String annotation : annotations) {
			String Id = annotation.split("\t")[0];
			String type = Id.substring(0, 1);
			int reqId = Integer.parseInt(idmap.get(Id).split(":")[0]);
			if (type.equals("T")) {
				String anntype = annotation.split("\t")[1];
				String annword = annotation.split("\t")[2];
				int leftlim = Integer.parseInt(anntype.split(" ")[1]) - startChars.get(reqId);
				int rightlim = Integer.parseInt(anntype.split(" ")[2]) - startChars.get(reqId);
				anntype = anntype.split(" ")[0] + " " + leftlim + " " + rightlim;
				annAnnotations += idmap.get(Id) + "\t" + anntype + "\t" + annword + "\n";
			} else if (type.equals("R")) {
				String anntype = annotation.split("\t")[1];
				anntype = anntype.split(" ")[0] + " " + idmap.get(anntype.split(" ")[1].split(":")[1]) + " "
						+ idmap.get(anntype.split(" ")[2].split(":")[1]);
				annAnnotations += idmap.get(Id) + "\t" + anntype + "\n";
			}
		}

		String ntext = "REQUIREMENTS\n------------\n";
		ntext += txtRequirements;
		ntext += "------------\n\nANNOTATIONS\n------------\n";
		ntext += annAnnotations;
		ntext += "------------\n";

		return ntext;
	}

	/**
	 * This is a test function that receives an rqs file, a txt file, and an ann file and executes transformations among
	 * these formats. The paths to the files must be given as arguments separated by space between them (e.g.
	 * "path/to/new_file.rqs path/to/new_file.txt path/to/new_file.ann")
	 * 
	 * @param args the arguments given to this function, which are the paths to the rqs, txt, and ann files.
	 */
	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(new File(args[0]));
			String RQS = "";
			while (scanner.hasNextLine()) {
				RQS += scanner.nextLine() + "\n";
			}
			scanner.close();
			String[] txtAndAnn = transformRQStoTXTandANN(RQS);
			System.out.println(txtAndAnn[0]);
			System.out.println(txtAndAnn[1]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Scanner txtScanner = new Scanner(new File(args[1]));
			String TXT = "";
			while (txtScanner.hasNextLine()) {
				TXT += txtScanner.nextLine() + "\n";
			}
			txtScanner.close();

			Scanner annScanner = new Scanner(new File(args[2]));
			String ANN = "";
			while (annScanner.hasNextLine()) {
				ANN += annScanner.nextLine() + "\n";
			}
			annScanner.close();

			String rqs = transformTXTandANNtoRQS(TXT, ANN);
			System.out.println(rqs);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
}
