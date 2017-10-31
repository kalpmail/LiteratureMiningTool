package edu.mayo.nlp.ie;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import edu.mayo.nlp.qa.Question;
import edu.mayo.nlp.summ.Sentence;

public class SemDB {

	public SemDB(String db) {
		try {
			conn = DriverManager.getConnection(db);
			/*conn = DriverManager.getConnection("jdbc:mysql://localhost/semmed2011?" +
					"user=root&password=root");*/
			/*conn = DriverManager.getConnection("jdbc:mysql://mysql.chpc.utah.edu/semmed2011?" +
					"user=semmed&password=xD4dk7Smr");*/
			 

		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	public Connection conn = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {


		try {
			//jdbc:mysql://mysql.chpc.utah.edu:3306/SemanticMedline?","semmed","xD4dK7Smr
			SemDB s = new SemDB("jdbc:mysql://research-db.nubic.northwestern.edu/semmeddb?" + "user=semmeddb-user&password=dab2_splines"); //Kalpana
			Statement stmt = s.conn.createStatement(); //Kalpana
			//Statement stmt = new SemDB("jdbc:mysql://rcfdb-dev:3306/semmedVER23?" +	"user=m093788&password=").conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM SENTENCE LIMIT 5");
			
			while (rs.next()) {
				System.out.println(rs.getString("SENTENCE"));
			}

		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

    public ArrayList<Sentence> getSentencesConclusions(ArrayList<String> pmids){
    	ArrayList<Sentence> sentences = new ArrayList<Sentence>();
    	String conclusionsQuery = 
    			"Select DISTINCT number, type, sentence, predicate, s_cui, s_name, o_cui, o_name"+
    					" from SENTENCE S LEFT JOIN PREDICATE_AGGREGATE P "+ 
    					" ON S.PMID = P.PMID AND S.sentence_ID = P.SID"+
    					" WHERE S.PMID=";
    	Statement stmt = null;
    	try {
    		for(int i=0;i<pmids.size();i++)
    		{
    			ArrayList<Sentence> entrySentences = new ArrayList<Sentence>(); 
    			stmt = conn.createStatement();
    			ResultSet rs = stmt.executeQuery(conclusionsQuery+"'"+pmids.get(i)+"' order by number");
    			boolean isstructured = false;
    			int startNumber=0,j=0;
    			while (rs.next()) {
    				String temp = rs.getString("SENTENCE");
    				Sentence sen = new Sentence(temp);
    				sen.pmid = Integer.parseInt(pmids.get(i));
    				sen.number = rs.getInt("number");
    				sen.sCUI = rs.getString("s_cui");
    				sen.sName = rs.getString("s_name");
    				sen.oCUI = rs.getString("o_cui");
    				sen.oName = rs.getString("o_name");
    				sen.predicate = rs.getString("predicate");
    				sen.title =rs.getString("type"); 
    				entrySentences.add(sen);
    				if(!isstructured)
    				{
    					if((temp.contains("CONCLUSIONS:"))||(temp.contains("CONCLUSION:"))
    							||(temp.contains("INTERPRETATION:"))||(temp.contains("INTERPRETATIONS:")))
    					{
    						isstructured = true;
    						startNumber = j;
    					}
    				}
    				j++;
    			}
    			StringBuilder summary = new StringBuilder();
    			int lastNumber =-1;
    			if(!isstructured)
    				startNumber = j-3;
    			while(startNumber<j)
    			{
    				Sentence temp = entrySentences.get(startNumber); 
    				if(temp.number!=lastNumber)
    				{
    					lastNumber = temp.number;
    					summary.append(temp.str);
    				}
    				startNumber++;
    			}
    			sentences.addAll(entrySentences);
    			Sentence summarySentence = new Sentence(summary.toString());
    			rs = stmt.executeQuery("SELECT DA FROM CITATIONS WHERE PMID ='"+pmids.get(i)+"'");
    			String pubDate = null;
    			while (rs.next()) 
    				pubDate = rs.getString(1);
    			if(pubDate!=null)
    				summarySentence.pubyear = pubDate.replaceAll(" ", "/");
    			sentences.add(summarySentence);
    		}
    	} catch (SQLException ex) {
    		// handle any errors
    		System.out.println("SQLException: " + ex.getMessage() + "\n in the query:\n");
    		System.out.println("SQLState: " + ex.getSQLState());
    		System.out.println("VendorError: " + ex.getErrorCode());
    	}finally{
    		try {
    			stmt.close();
    			conn.close();
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    	return sentences;
    }

	public ArrayList<Sentence> getJournals(ArrayList<String> pmids) {//Soheil
		ArrayList<Sentence> sentences = new ArrayList<Sentence>();

		/*sentences.addAll(askSemMed_OnlyArticleInfo("SELECT DISTINCT Pmid, Year, Journal_Title, Issn, Publication_Type, Study_Design, final_score FROM ARTICLE_INFO\n" + 
				"WHERE PMID IN("+StringUtils.join(pmids.iterator(), ",") + ");")); */ //Kalpana

		return sentences;
	}

	private Collection<? extends Sentence> askSemMed_OnlyArticleInfo(String query) {//Soheil
		ArrayList<Sentence> sentences = new ArrayList<Sentence>();
		try {
			Statement stmt = conn.createStatement();
			System.out.println(query);
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				Sentence sen = new Sentence("");
				sen.pmid = Integer.parseInt(rs.getString("PMID"));
				sen.sCUI = "";
				sen.sName = "";
				sen.oCUI = "";
				sen.oName = "";
				/*sen.predicate = "";
				sen.year = rs.getString("Year");
				sen.journalTitle = rs.getString("Journal_Title");
				sen.publicationType = rs.getString("Publication_Type");
				sen.issn = rs.getString("Issn");
				sen.studyDesign = rs.getString("Study_Design");
				sen.finalScore = rs.getDouble("final_score");*/ //Kalpana
				
				sentences.add(sen);
			}
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage() + "\n in the query:\n"+query);
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return sentences;
	}
	
	public ArrayList<Sentence> getSentences(Question q, ArrayList<String> pmids) {
		ArrayList<String> quotedPmids = new ArrayList<String>();
		for(String pmid : pmids){
			quotedPmids.add(String.format("'%s'", pmid));
		}
		
		
		ArrayList<Sentence> sentences = new ArrayList<Sentence>();

		//specific
		String predicate = null;
		if(q.type.equals("treatment"))
			predicate = "TREATS";

		ArrayList<Integer> subjects = new ArrayList<Integer>(); 
		ArrayList<Integer> objects = new ArrayList<Integer>();


		for(int i=0; i< q.mentions.size(); i++){
			if(q.eligibilities.get(i)){
				if(q.umlsSemGrps.get(i).contains("DRUG")||q.umlsSemGrps.get(i).contains("CHEM")||q.umlsSemGrps.get(i).contains("PROC")){//specific
					subjects.add(i);
				}
				else if(q.umlsSemGrps.get(i).contains("DISO") ||  q.umlsSemGrps.get(i).contains("FIND")){//specific
					objects.add(i);
				}					
			}
		}

		//scenario 1: No subject
		if(subjects.size()==0 && objects.size()!=0){
			ArrayList<String> umlsCuis = new ArrayList<String>();
			for (Integer i : objects) {
				q.print(i);
				for (String str : q.umlsCodes.get(i).split(";")) {
					umlsCuis.add("'"+str.split("_")[0]+"'");
				}				
			}
			sentences.addAll(askSemMed(selectfrom +
					"WHERE PREDICATION_AGGREGATE.PMID IN("+StringUtils.join(quotedPmids.iterator(), ",")+") AND\n" +
					"predicate = '"+predicate+"' AND o_CUI IN("+StringUtils.join(umlsCuis.iterator(), ",")+") AND s_novel = 1\n" +
					joiner));

		}


		//scenario 2: at least one object and one subject
		else if(subjects.size()!=0 && objects.size()!=0){
			ArrayList<String> sbjUmlsCuis = new ArrayList<String>();
			for (Integer i : subjects) {
				q.print(i);
				for (String str : q.umlsCodes.get(i).split(";")) {
					sbjUmlsCuis.add("'"+str.split("_")[0]+"'");
				}				
			}
			ArrayList<String> objUmlsCuis = new ArrayList<String>();
			for (Integer i : objects) {
				q.print(i);
				for (String str : q.umlsCodes.get(i).split(";")) {
					objUmlsCuis.add("'"+str.split("_")[0]+"'");
				}				
			}

			sentences.addAll(askSemMed(selectfrom +
					"WHERE PREDICATION_AGGREGATE.PMID IN("+StringUtils.join(quotedPmids.iterator(), ",")+") AND\n" +
					"predicate = '"+predicate+"' AND o_CUI IN("+StringUtils.join(objUmlsCuis.iterator(), ",")+") AND " +
					"s_CUI IN("+StringUtils.join(sbjUmlsCuis.iterator(), ",")+")\n" +
					joiner));
			
			if(sentences.size()<SemDB.MIN_SENTENCES){
				sentences.addAll(askSemMed(selectfrom +
						"WHERE PREDICATION_AGGREGATE.PMID IN("+StringUtils.join(quotedPmids.iterator(), ",")+") AND\n" +
						"predicate = '"+predicate+"' AND s_CUI IN("+StringUtils.join(sbjUmlsCuis.iterator(), ",")+") AND o_novel = 1\n" +
						joiner));
				sentences.addAll(askSemMed(selectfrom +
						"WHERE PREDICATION_AGGREGATE.PMID IN("+StringUtils.join(quotedPmids.iterator(), ",")+") AND\n" +
						"predicate = '"+predicate+"' AND o_CUI IN("+StringUtils.join(objUmlsCuis.iterator(), ",")+") AND s_novel = 1\n" +
						joiner));
			}

		}

		//scenario 3: at least one object and one subject -- this is a special case 
		//TODO: approximate search
		else if(subjects.size()!=0 && objects.size()==0){
			ArrayList<String> umlsCuis = new ArrayList<String>();
			for (Integer i : subjects) {
				q.print(i);
				for (String str : q.umlsCodes.get(i).split(";")) {
					umlsCuis.add("'"+str.split("_")[0]+"'");
				}				
			}
			sentences.addAll(askSemMed(selectfrom +
					"WHERE PREDICATION_AGGREGATE.PMID IN("+StringUtils.join(quotedPmids.iterator(), ",")+") AND\n" +
					"predicate = '"+predicate+"' AND s_CUI IN("+StringUtils.join(umlsCuis.iterator(), ",")+") AND o_novel = 1\n" +
					joiner));
			
		}
		
		else{
			for(int i=0; i< q.mentions.size(); i++){
				sentences.addAll(askSemMed(selectfrom +
						"WHERE PREDICATION_AGGREGATE.PMID IN("+StringUtils.join(quotedPmids.iterator(), ",")+") AND\n" +
						"predicate = '"+predicate+"' AND (o_name = '"+q.norms.get(i)+"' OR o_name = '"+q.mentions.get(i)+"') AND s_novel = 1\n" +
						joiner));
			}
		}
		if(sentences.size()==0) System.err.println("sorry, use semantic search. or go through the pmids");
		
		return sentences;

	}

	static final int MIN_SENTENCES = 10;
	static final String selectfrom = 
			//"SELECT DISTINCT SENTENCE, PREDICATION_AGGREGATE.PMID, s_cui, s_name, o_cui, o_name, predicate, Year, Journal_Title, Issn, Publication_Type, Study_Design, final_score FROM PREDICATION_AGGREGATE, SENTENCE, ARTICLE_INFO\n";//Soheil 
			"SELECT DISTINCT SENTENCE, PREDICATION_AGGREGATE.PMID, predicate, s_cui, s_name, o_cui, o_name FROM PREDICATION_AGGREGATE, SENTENCE\n"; //Kalpana - Removed Field-Year, Journal_Title, Issn, Publication_Type, Study_Design, final_score and Table-Article_Info 
			//"SELECT DISTINCT SENTENCE, PREDICATION_AGGREGATE.PMID, s_cui, s_name, o_cui, o_name, predicate, SUBJECT_START_INDEX, SUBJECT_END_INDEX, PREDICATE_START_INDEX, PREDICATE_END_INDEX FROM PREDICATION_AGGREGATE, SENTENCE, sentence_predication\n";
	static final String joiner = 
			//"AND SENTENCE.SENTENCE_ID=SID AND PID=PREDICATION_ID AND PNUMBER=PREDICATION_NUMBER";
			"AND SENTENCE.SENTENCE_ID=SID"; //AND SENTENCE.PMID=ARTICLE_INFO.Pmid"; //Kalpana
	
	private Collection<? extends Sentence> askSemMed(String query) {
		ArrayList<Sentence> sentences = new ArrayList<Sentence>();
		try {
			Statement stmt = conn.createStatement();
			System.out.println(query);
			ResultSet rs = stmt
					.executeQuery(query);
			
			while (rs.next()) {
				Sentence sen = new Sentence(rs.getString("SENTENCE"));
				sen.pmid = Integer.parseInt(rs.getString("PMID"));
				sen.sCUI = rs.getString("s_cui");
				sen.sName = rs.getString("s_name");
				sen.oCUI = rs.getString("o_cui");
				sen.oName = rs.getString("o_name");
				sen.predicate = rs.getString("predicate");
				/*sen.year = rs.getString("Year");//Soheil
				sen.journalTitle = rs.getString("Journal_Title");//Soheil
				sen.publicationType = rs.getString("Publication_Type");//Soheil
				sen.issn = rs.getString("Issn");//Soheil
				sen.studyDesign = rs.getString("Study_Design");//Soheil
				sen.finalScore = rs.getDouble("final_score");//Soheil
				sen.subjStart = rs.getString("SUBJECT_START_INDEX");
				sen.subjEnd = rs.getString("SUBJECT_END_INDEX");
				sen.objStart = rs.getString("PREDICATE_START_INDEX");
				sen.objEnd = rs.getString("PREDICATE_END_INDEX");
				*/

				sentences.add(sen);
			}
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage() + "\n in the query:\n"+query);
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return sentences;
	}

	public ArrayList<String> filterPmids(ArrayList<String> pmids,ArrayList<String> predicateList) {
		ArrayList<String> newpmids  = new ArrayList<String>();
		String query = "SELECT DISTINCT PMID FROM PREDICATE_AGGREGATE WHERE PMID IN ("
				+StringUtils.join(pmids.iterator(), ",")+") AND predicate IN ("+
				StringUtils.join(predicateList.iterator(), ",")+")"; 
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				newpmids.add(rs.getString(1));
			}
		}
		catch(SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage() + "\n in the query:\n"+query);
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}finally{
			try {
				stmt.close();
				conn.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return newpmids;
	}

}
