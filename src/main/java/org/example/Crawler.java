package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

public class Crawler {
    private HashSet<String> urlLink ;
    private  int MAX_DEPTH = 2;
    public Connection connection ;
    public Crawler(){
        //set utp the connection to mysql
        connection = DatabaseConnection.getConnection();
        urlLink = new HashSet<String>();
    }
    //recursive method
    public void getPageTextAndLinks(String url , int depth){
        if(!urlLink.contains(url)){
            if(urlLink.add(url)){
                System.out.println(url);
            }
            try{
                //parsing HTMl object to java Documents object
                Document document = Jsoup.connect(url).timeout(5000).get();
                //Get text from documents object
                String text = document.text().length()<500 ? document.text():document.text().substring(0,499);
                //Printing text
                System.out.println(text);

                //Insert data into pages table....Prepared Statement help us to write only inset command
                PreparedStatement preparedStatement = connection.prepareStatement("Insert into pages values(?,?,?)");
                preparedStatement.setString(1,document.title());
                preparedStatement.setString(2,url);
                preparedStatement.setString(3,text);
                preparedStatement.executeUpdate();

                //increase depth
                depth++;
                //if depth is greater than max then return
                if(depth > MAX_DEPTH){
                    return ;
                }
                // Get hyperlink available on the current page
                Elements availableLinksOnPage = document.select("a[href]");
                //run method recursively for every link available on current page
                for(Element currentLink: availableLinksOnPage){
                    getPageTextAndLinks(currentLink.attr("abs:href"),depth);
                }
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }

            catch(SQLException sqlException){
                sqlException.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
       Crawler crawler = new Crawler();
       crawler.getPageTextAndLinks("https://www.javatpoint.com/",0);
    }
}