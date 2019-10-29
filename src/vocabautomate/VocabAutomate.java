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
		for(String quiz : quizlets) {
			ArrayList<String> definitions = myMethods.downloadQuizlet(quiz);
			Collections.sort(definitions);
				
			output.addAll(myMethods.compareTerms(terms, definitions));
		}
		
		for(String term : terms) {
			System.out.println(term);
		}
		
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
		
		int termsOriginalSize = terms.size();
		
		int jumpVal = 0;
		for(int i = 0; i < termsOriginalSize; i++) {
			for(int j = jumpVal; j < definitions.size(); j++) {
				if(!terms.get(i).startsWith("DEMONS") && definitions.get(j).startsWith(terms.get(i))) {
					compared.add(definitions.get(j));
					terms.set(i, "DEMONS");
					break;
				}
			}
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
