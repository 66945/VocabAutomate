package vocabautomate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VocabAutomate {
	
	
	//Read the README documentation
	public static void main(String[] args) throws IOException {
		VocabAutomate myMethods = new VocabAutomate();
		Scanner sc = new Scanner(System.in);
		
		ArrayList<String> output = new ArrayList<>();
		
		System.out.println("Input File:");
		ArrayList<String> terms = myMethods.readVocabWords(new File(sc.nextLine()));
		Collections.sort(terms);
		
		System.out.println("Quizlet Search Term:");
		ArrayList<String> quizlets = myMethods.searchQuizlets(sc.nextLine());
		
		ArrayList<String> definitions = new ArrayList<>();
		for(String quiz : quizlets) {
			definitions.addAll(myMethods.downloadQuizlet(quiz));				
		}
		
		Collections.sort(definitions);
		output.addAll(myMethods.compareTerms(terms, definitions));
		
		System.out.println("Output File:");
		myMethods.writeToFile(output, new File(sc.nextLine()));
		
		sc.close();
	}
	
	//Takes a search term subject and searches quizlet for related quizi (What the heck is the plural for quiz?)
	//It returns the links to these quizi
	public ArrayList<String> searchQuizlets(String subject) throws IOException {
		ArrayList<String> searchResults = new ArrayList<>();
		
		String url = "https://www.quizlet.com/subject/" + subject.replace(' ', '-') + "/";
		
		Document document = Jsoup.connect(url).get();
		Elements links = document.getElementsByClass("UILink");
		
		String flashCardLinkPattern = "https:\\/\\/quizlet\\.com\\/\\d*\\/[\\w-]*\\/";
		
		for(Element link : links) {
			if(Pattern.matches(flashCardLinkPattern, link.attr("href")))
				searchResults.add(link.attr("href"));
		}
		
		//Print statements for debugging
		for(String result : searchResults) {
			System.out.println(result);
		}
		
		return searchResults;
	}
	
	//Returns all the definitions provided in the quiz that the url refers to
	public ArrayList<String> downloadQuizlet(String url) throws IOException {		
		Document document = Jsoup.connect(url).get();
		
		Elements termsElements = document.getElementsByClass("SetPageTerms-term");
		
		ArrayList<String> quizletTerms = new ArrayList<>();
		
		for(Element quizTerm : termsElements) {
			quizletTerms.add(quizTerm.text().toLowerCase());
		}
			
		return quizletTerms;
	}
	
	//returns the vocab words you want defined from a file
	public ArrayList<String> readVocabWords(File vocabFile) throws FileNotFoundException {
		ArrayList<String> terms = new ArrayList<>();
		
		Scanner sc = new Scanner(vocabFile);
		while(sc.hasNext()) {
			terms.add(sc.nextLine().toLowerCase());
		}
		sc.close();
		
		return terms;
	}
	
	
	//weeds the definitions and the terms and then only returns the definitions you want
	public ArrayList<String> compareTerms(ArrayList<String> terms, ArrayList<String> definitions) {
		ArrayList<String> compared = new ArrayList<>();
		ArrayList<String> leftOver = new ArrayList<>(); //the terms that don't have definitions and can't be compared. Find a better var name later
		
		//A temporary value that saves what definition the loops stop at after each term
		//This helps reduce the redundant checks.
		int saveVal = 0;
		
		for(int i = 0; i < terms.size(); i++) {			
			String currentTerm = terms.get(i);
			
			for(int j = saveVal; j < definitions.size(); j++) {
				String currentDef = definitions.get(j);
				
				//Check if the definition matches the term
				if(currentDef.startsWith(currentTerm)) {
					//sets the saveVal so that when the program breaks out of the loop,
					//it will iterate both the term and definition
					saveVal = j + 1;
					
					//adds the matching definition to the list of successfully found definitions
					compared.add(currentDef);
					break;
				}
				
				//checks if this is the final value.
				//if it is, and the program has gotten this far, the term is not in
				//the definitions. We add it to leftOver and move on.
				//
				//Because the saveVal is only incremented to j+1 if the program finds a match,
				//the counter will be reset to whatever it was after the previous term.
				if(j + 1 == definitions.size()) {
					leftOver.add(currentTerm);
				}
			}
		}
		
		//Print statements for debugging
		for(String comparison : compared) {
			System.out.println(comparison);
		}
		
		System.out.println("Failed terms:");
		
		for(String left : leftOver) {
			System.out.println(left);
		}
		
		return compared;
	}
	
	//writes an arraylist to a file
	public void writeToFile(ArrayList<String> finalTerms, File destination) throws IOException {
		FileWriter fw = new FileWriter(destination);
		PrintWriter pw = new PrintWriter(fw);
		
		for(String finalTerm : finalTerms) {
			pw.println(finalTerm);
		}
		
		pw.close();
	}
}
