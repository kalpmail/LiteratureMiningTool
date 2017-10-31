package edu.mayo.nlp.ir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.mayo.nlp.qa.Logger;
import edu.mayo.nlp.qa.Question;

public class EUtilsExtractor {
	
	public EUtilsExtractor() {
		Date date = new Date();  
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -365*YEARS_BACK);
		fromDate = dateFormat.format(cal.getTime());
		toDate = dateFormat.format(date);
		/*prefix+="\""+fromDate+"\"[PDat]:\""+toDate+"\"[PDat] AND " 
				+"(hasabstract[text] AND English[lang] AND \"humans\"[MeSH Terms]) AND "
				;*/
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException, IOException {
		String query="Therapy/Broad[filter] AND \"Weight Loss\"[MeSH]+AND+\"Obesity\"[MeSH]+AND+\"Osteoarthritis\"[MeSH]";
		System.out.println(query);
		ArrayList<String> pmids = new EUtilsExtractor().getPmids(query);
		for (String pmid : pmids) {
			System.out.print(pmid+",");
		}
		
	}
	
	public ArrayList<String> getPmids(String query) {
		String url = prefix+query.replaceAll("\\s+", "+")+"+AND+(\""+fromDate+"\"[PDat]:\""+toDate+"\"[PDat])";
		System.out.println(url);
		int count=-1;ArrayList<String> pmids=new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
			while(br.ready()){
				String line=br.readLine();
				//System.out.println(line);
				if(count<0){
					Matcher m = p_count.matcher(line);
					if(m.find()){
						count=Integer.parseInt(m.group(1));
						//System.out.println("count:"+count);
					}					
				}
				
				Matcher m = p.matcher(line);
				if(m.find()){
					pmids.add(m.group(1));
				}
					//
			}
			br.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pmids;
	}


	public static final int MAX_PMIDS = 100; //100000; //100; 
	
	//branch 'master' of https://github.com/sidkgp/KnowledgeSummary.git
	public ArrayList<String> getPmids(Question q, String andor) {
		HashSet<String> pmids = new HashSet<String>(); 
		
		if(q.type.equals("treatment")){//for treatment restrict only to semantic types
			String query = "";
			ArrayList<String> meshs = new ArrayList<String>();
			
			for(int i=0; i< q.mentions.size(); i++){
				if(q.eligibilities.get(i)){
					//System.err.println(q.meshCodes.get(i));
					meshs.add("\""+q.meshCodes.get(i)+"\"[MeSH]");
				}
			}
			if(meshs.size()!=0) 
				query+=StringUtils.join(meshs.iterator(), "+"+andor+"+");
			else
				query=q.question;
			pmids.addAll(getPmids("systematic[sb] AND ("+query+")"));
			pmids.addAll(getPmids("Therapy/Narrow[filter] AND ("+query+")"));
			if(pmids.size()<MAX_PMIDS)
				pmids.addAll(getPmids("Therapy/Broad[filter] AND ("+query+")"));
			
		}
		
		if(pmids.size()<MAX_PMIDS&&!andor.equals("OR")) {System.err.println("going for the or search"); return getPmids(q, "OR");}
		if(pmids.size()<MAX_PMIDS){
			String query = q.question;
			pmids.addAll(getPmids("systematic[sb] AND ("+query+")"));
			pmids.addAll(getPmids("Therapy/Narrow[filter] AND ("+query+")"));
			if(pmids.size()<MAX_PMIDS)
				pmids.addAll(getPmids("Therapy/Broad[filter] AND ("+query+")"));
			if(pmids.size()<MAX_PMIDS)
				pmids.addAll(getPmids(query));
		}
		
		
		ArrayList<String > ret = new ArrayList<String>(pmids);
		Collections.sort(ret);
		return ret;
	}
	
	public String spellChecker(String query){//Soheil
		System.out.println("Spell checking started for query: " + query);
		Logger.writeLn("Spell checking started for query: " + query);
		String xmlContent = "";
		try {
			URL url = new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/espell.fcgi?db=pubmed&term=" + query.replace(" ", "+"));
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			while(br.ready()){
				xmlContent += br.readLine();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(!xmlContent.isEmpty()){
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				InputSource is = new InputSource(new StringReader(xmlContent));
				Document doc = docBuilder.parse(is);
				NodeList lstCorrectedQuery = doc.getElementsByTagName("CorrectedQuery");
				if(lstCorrectedQuery.getLength() >= 1){
					String newQuery = lstCorrectedQuery.item(0).getTextContent();
					System.out.println("The query \"" + query + "\" has been corrected to \"" + newQuery + "\"");
					Logger.writeLn("The query \"" + query + "\" has been corrected to \"" + newQuery + "\"");
					return newQuery;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	Pattern p_count = Pattern.compile("<Count>(\\d+)</Count>");
	Pattern p = Pattern.compile("<Id>(\\d+)</Id>");
	final int YEARS_BACK = 10; //41;  //10;
	String fromDate,toDate;
	String prefix = "http://www.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?" +
			"db=pubmed&retmax=100000&retmode=xml&term=";
}
