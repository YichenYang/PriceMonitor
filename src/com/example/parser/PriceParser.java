package com.example.parser;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PriceParser {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public void run(String args) {
		// read initial condition
		String searchItem = null;
		String pageNum = "0";
		System.out.println("Fetching...");
		
			searchItem = args;
			pageNum = "1";
			int pageNumInt = Integer.parseInt(pageNum);
			pageNumInt = 16*(pageNumInt-1);
			pageNum = Integer.toString(pageNumInt);
			
			try {
				String totalNumberofItem = Query1(searchItem);
				int totalNumberodItem = Integer.parseInt(totalNumberofItem);
				if (totalNumberodItem < pageNumInt)
					System.err.println("There are not enough items to list on this page index !");
				else{
					ArrayList<resultItem> prodResultItems = Query2(searchItem, pageNum);
					for (resultItem e:prodResultItems){
						System.out.println("Product: " + e.name);
						System.out.println("Price: " + e.price);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	private static String Query1(String searchItem) throws IOException{
		searchItem = searchItem.replaceAll(" +", "+");
		String url = "http://www.walmart.com/search/search-ng.do?tab_value=all&search_query=" +
				searchItem +
				"&search_constraint=0&Find=Find&pref_store=3795&ss=false&ic=16_0&_mm=";
		Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) " +
				"AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
		
		Elements result = doc.select("span[class=floatLeft numResults mt5]");
		String resultString = result.text();
		String[] resultStringArray = resultString.split(" "); 
		return resultStringArray[0];
	}
	
	public ArrayList<resultItem> Query2(String searchItem, String pageNumber) 
			throws IOException{
		ArrayList<resultItem> returnItems = new ArrayList<resultItem>();
		searchItem = searchItem.replaceAll(" +", "+");
		String url = "http://www.walmart.com/search/search-ng.do?tab_value=all&search_query=" +
				searchItem +
				"&search_constraint=0&Find=Find&pref_store=3795&ss=false&ic=16_" +
				pageNumber + "&_mm=";
		Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) " +
				"AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
			
		Elements result = doc.select("div.prodInfo");
		for (Element e:result){
			resultItem tempItem = new resultItem();
			tempItem.name = e.getElementsByAttribute("title").attr("title");
			if (!e.getElementsByClass("camelPrice").isEmpty())
				tempItem.price = e.getElementsByClass("camelPrice").text();
			else if(!e.getElementsByClass("PriceLBold").isEmpty()){
				tempItem.price = e.getElementsByClass("PriceLBold").text();
			}else{
				tempItem.price = "Not Available";
			}
			returnItems.add(tempItem);
		}
		return returnItems;
	}
}
