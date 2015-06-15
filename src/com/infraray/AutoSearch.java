package com.infraray;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;



import com.infraray.GoogleSearch;
import com.infraray.AllGoogleSearch;


public class AutoSearch {
	
	private static boolean checkArgs(String[] args) {
		
		if (args.length<2) {
			System.out.println("Usage: java -jar AutoSearch [inpath] [outpath]");
			return false;
		}
		return true;
	}
	
    public static void main(String[] args)  {

    	if (!checkArgs(args)) return;
    	
    	AllGoogleSearch all_gs = new AllGoogleSearch();
    	all_gs.doAllSearch(args[0]);
    	
    	
    	int rows = all_gs.saveResults(args[1]);
	
    	if (rows < 0) {
    		System.out.println("Save outout file error! ");
    		//return -1;
    	} else
    		System.out.println(String.format("Total lines: %d", rows));

    	//return rows;
        
    }
}
