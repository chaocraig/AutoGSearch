package com.infraray;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.infraray.GoogleSearch;



public class AllGoogleSearch {

	final static String google_url="https://www.google.com.tw/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8#q";

	
	static List <String> listDates = new ArrayList<String>();	
	static List <String> listJulianDates = new ArrayList<String>();
	static List <String> listKeywords = new ArrayList<String>();
	
	static List <String> listOutput = new ArrayList<String>();
	
	GoogleSearch gs = new GoogleSearch();
	
	public boolean readSearchKeywordsOptions(String inpath)  { //[0] input, [1] output
		
		Reader in=null;
		try {
			in = new FileReader(inpath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		Iterable<CSVRecord> records=null;
		try {
			records = CSVFormat.EXCEL.withHeader().parse(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
		for (CSVRecord record : records) {
		    String begin_date = record.get("begin_date");
		    String end_date = record.get("end_date");

		    
		    if ( (begin_date.equals("") || end_date.equals("")) ) {
		    	listJulianDates.add("-");
		    } else if ( (begin_date.equals("O") || end_date.equals("O")) ) {
		    	listJulianDates.add("O");
		    	listDates.add("O");
		    } else if ( (begin_date.equals("X") || end_date.equals("X")) ) {
		    	listJulianDates.add("X");
		    } else {
		    	listDates.add(String.format("%s-%s", begin_date, end_date) );
			    String strJulianDate = String.format("%s-%s", gs.Date2Julian(begin_date), gs.Date2Julian(end_date));		    
			    listJulianDates.add(strJulianDate);
			    System.out.println(strJulianDate);		    			    	
		    }
		    
		    String c1 = record.get("c1").trim();
		    String c2 = record.get("c2").trim();
		    String c3 = record.get("c3").trim();
		    
		    if ( !(c1.equals("") && c2.equals("") && c3.equals("")) ) {
			    //String strKeywords = String.format("\"%s\"+\"%s\"+\"%s\"", c1, c2, c3 );
			    String strKeywords = String.format("%s+%s+%s", c1, c2, c3 );
			    listKeywords.add(strKeywords);
			    System.out.println(strKeywords);			    	
		    }

		}
		
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	
	public void doAllSearch(String inpath) {
		
		if (!readSearchKeywordsOptions(inpath)) return;
		
		
		for (String strKeyword : listKeywords) {
			List <String> listColumns = new ArrayList(); //store ResultStat with the same keyword and different time range
			String strCSV =  strKeyword + ",";
			
			String strSearch="";
			for (String strDateRange : listJulianDates) {
				if (strDateRange.equals("O")) {
					strSearch = String.format("%s=%s", google_url, strKeyword);					
				} else if (strDateRange.equals("X")) { 
					break;
				} else if (strDateRange.equals("-")) { 
					continue;
				} else {
					strSearch = String.format("%s=%s daterange=%s", google_url, strKeyword, strDateRange);					
				}

				System.out.println(strSearch);
				
				
				gs.doSearch(strSearch);
					
				//String strStat = gs.getResultStatString();			
				//String strStat = gs.getJsoupResultStat();
				
				String strStat = gs.getFileResultStats();
				
				if (strStat.equals("")) {
					System.out.println("Error: cannot get resultStat!");
				} else if (strStat.equals("X")) {
					manageBlocked();
				} 
				
				System.out.println("====> resultStats: " + strStat);
				listColumns.add(strStat);
				
			}
			
			if (listColumns==null) {
				System.out.println("Error: no search results!");
				return;
			}
			
			for (String stat : listColumns) { //combine as a row of columns
				strCSV = strCSV + stat + "," ; 
			}
			strCSV = strCSV.replaceAll("\"", "");
			strCSV = strCSV + "\n";
			
			listOutput.add(strCSV);
		}
		
	
	}
	
	public int saveResults(String outpath) {
		
		
		CSVFormat format = CSVFormat.newFormat('t')
			    .withCommentMarker('#')
			    .withIgnoreEmptyLines(true)
			    .withNullString("")
			    .withHeader();

		try {
			FileWriter fileWriter = new FileWriter(outpath);
			
			String strHeaders = "Keywords";
			for (String strDate : listDates) {
					strHeaders= strHeaders + "," + strDate;
			}
			
			fileWriter.write(strHeaders +"\n");
			
			for (String rec : listOutput) {
				fileWriter.write(rec);
			}
					
            fileWriter.close();
            
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		} 	
		
		return(listOutput.size());
		
	}

	
	public void manageBlocked() {
		System.out.println("Sorry, we are blocked by Google!!!\nWait 90 seconds for recover...");
		
		try {
			Process p = Runtime.getRuntime().exec("./adsl_reboot.sh");
			try {
				Thread.sleep(90000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
