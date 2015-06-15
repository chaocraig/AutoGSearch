package com.infraray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptExecutor;

public class GoogleSearch {
	
	private static String savedHtmlPath = "./gs.html";
	private static Platform[] listPlatform = {Platform.WINDOWS, Platform.XP, Platform.VISTA, 
		Platform.WIN8, Platform.ANDROID, Platform.LINUX, Platform.MAC, Platform.SNOW_LEOPARD, Platform.MOUNTAIN_LION};
	

	private static WebDriver driver = null;
	
	public WebDriver createWebDriver() {
		
		int platform_no = listPlatform.length;
		Random rand = new Random(); 
    	
		Platform platform = listPlatform[rand.nextInt(platform_no)];
		String ver = String.format("%d", rand.nextInt(platform_no));
		System.out.println("platform = " + platform + ", Version = " + ver);
		
    	DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
    	capabilities.setBrowserName("Chrome");
    	capabilities.setPlatform(platform);
    	capabilities.setVersion(ver);
    	WebDriver driver = new HtmlUnitDriver(capabilities);    
    	this.driver = driver;
  
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS); //implicit wait
		
        return (driver);
	}
	
	public void doSearch(String url) {
		
		WebDriver driver = createWebDriver();
        driver.get(url);
        try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //driver.get(google_url + "html");
        //WebElement HtmlSrc=driver.findElement(By.tagName("html"));
        String HtmlSrc = driver.getPageSource();
     
        
        try (PrintStream out = new PrintStream(new FileOutputStream(savedHtmlPath))) {
            out.print(HtmlSrc);
            System.out.println("Saved HTML file to ..." + savedHtmlPath );
            out.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }
        
	}
	
	
	/*
	public String getResultStatString() {

        WebElement resultStat = driver.findElement(By.id("resultStats")); 
        resultStat.click();
        //resultStat.notifyAll();
        
        try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        WebElement mydiv = new WebDriverWait(driver, 10).until(new ExpectedCondition<WebElement>() {
		      public WebElement apply(WebDriver from) {
		          try {
		        	WebElement element = from.findElement(By.id("resultStats"));
		            if (!"".equals(element.getText())) {
		            	return element;
		            }
		            return null;
		          } catch (StaleElementReferenceException e) {
		            return null;
		          }
		        }
		});
        
        String strResultStatText = resultStat.getText();
        //String strResultStatText = ((JavaScriptExecutor)driver).executeScript("return document.getElementById('resultStats').innerText;");
        
        if (strResultStatText.equals("")) return("");
        
        String aResultStat[] = strResultStatText.split(" ");
        String strResultStat = aResultStat[1].replaceAll(" ", ""); //strip characters of space (and comma)
        System.out.println("---> " + strResultStat);
        
        return(strResultStat);
	}
	*/
	
	public void showSearchResultLines() {
	    List<WebElement> resultLinks= driver.findElements(By.xpath("//h3[@class='r']/a[@href]"));
	    System.out.println("Results: " + resultLinks.size() );
	    int i = 1;
	    for (WebElement link : resultLinks) {
	    	 String title = link.getText();
	    	 String url   = link.getAttribute("href");
	    	 System.out.println("["+i+"]"+title+"["+url+"]");
	    	 i++;
	    }		
	}
	
	
	public String Date2Julian(String strDate) {
		
		int JGREG= 15 + 31*(10+12*1582);
		String strJulian;
		
	    int year = Integer.parseInt(strDate.substring(0, 4));
	    int month = Integer.parseInt(strDate.substring(4, 6));
	    int day = Integer.parseInt(strDate.substring(6, 8));
	    
	    int julianYear = year;
	    if (year < 0) julianYear++;
	    int julianMonth = month;
	    if (month > 2) {
	      julianMonth++;
	    }
	    else {
	      julianYear--;
	      julianMonth += 13;
	    }

	    double julian = (java.lang.Math.floor(365.25 * julianYear)
	         + java.lang.Math.floor(30.6001*julianMonth) + day + 1720995.0);
	    if (day + 31 * (month + 12 * year) >= JGREG) {
	      // change over to Gregorian calendar
	      int ja = (int)(0.01 * julianYear);
	      julian += 2 - ja + (0.25 * ja);
	    }
	    strJulian = Integer.toString((int)julian);
		
		return(strJulian);
		
	}

	
	public String getFileResultStats() {

        String line="";
        try {
            FileReader reader = new FileReader(savedHtmlPath);
            BufferedReader bufferedReader = new BufferedReader(reader);

            while ((line = bufferedReader.readLine()) != null) {
            	
            	if (line.indexOf("To continue, please type") != -1) { //blocked by Google
            		return "X"; //need manageBlocked();
            	}
            	if ( line.indexOf("<div class=\"sd\" id=\"resultStats\">") == -1) continue;
            	

            	//matched, next line is </div> or number of resultStats
            	line = bufferedReader.readLine().trim(); //text of resultStats
            	break;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    	if (line==null) return "?";  //error, no text
    	if (line.length()==0) return "??";  //error, no text
    	if (line.equals("</div>")) return "00";
        System.out.println(line);
        
        String aResultStat[] = line.split(" ");
        String strResultStat = aResultStat[1].replaceAll(" |,", ""); //strip characters of space and comma
        System.out.println("---> " + strResultStat);
        
        return(strResultStat);   
	}
	
	
	
	public String getJsoupResultStats() {
	


		File input = new File(savedHtmlPath);
		Document doc;
		try {
			doc = Jsoup.parse(input, "UTF-8", "https://www.google.com/");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		
		Elements resultStat = doc.select("div#resultStats");
		String strResultStat = resultStat.text();
		return strResultStat;
	}
	
	
}
