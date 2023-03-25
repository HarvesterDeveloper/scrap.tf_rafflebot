package org.harvestdev;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

public class Main
{
    public static void main(String[] args) throws InterruptedException, IOException {
        //region vars declaration

        WebDriver webdriver = null;
        ChromeOptions options = null;
        Scanner scanner = null;
        JavascriptExecutor javascriptExecutor = null;
        Random random = null;
        Document parsedWebPage = null;
        Elements raffleDivs = null;
        int programIterations = 1;

        //endregion

        //region program iterations set

        System.out.println("Enter number of program iterations(how many times you need to start search and join raffles again)");
        scanner = new Scanner(System.in);
        programIterations = scanner.nextInt();

        //endregion

        // region chrome webdriver settings

        URL url = Main.class.getClassLoader().getResource("drivers/chromedriver.exe");
        FileOutputStream output = new FileOutputStream("chromedriver.exe");
        InputStream input = url.openStream();
        byte [] buffer = new byte[4096];
        int bytesRead = input.read(buffer);
        while (bytesRead != -1) {
            output.write(buffer, 0, bytesRead);
            bytesRead = input.read(buffer);
        }
        output.close();
        input.close();

        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");

        try
        {
            options = new ChromeOptions();
            options.addArguments("--remote-allow-origins=*");
            webdriver = new ChromeDriver(options);
        }
        catch (Exception exception)
        {
            System.out.println(exception.getMessage());
            System.exit(0);
        }

        //endregion

        // region cookie input

        webdriver.get("https://scrap.tf/raffles/ending");
        webdriver.manage().deleteAllCookies();

        System.out.println("__asc:");
        webdriver.manage().addCookie(new Cookie("__asc", scanner.next()));
        System.out.println("__auc:");
        webdriver.manage().addCookie(new Cookie("__auc", scanner.next()));
        System.out.println("__cf_bm:");
        webdriver.manage().addCookie(new Cookie("__cf_bm", scanner.next()));
        System.out.println("_ga:");
        webdriver.manage().addCookie(new Cookie("_ga", scanner.next()));
        System.out.println("_gid:");
        webdriver.manage().addCookie(new Cookie("_gid", scanner.next()));
        System.out.println("_pbjs_userid_consent_data:");
        webdriver.manage().addCookie(new Cookie("_pbjs_userid_consent_data", scanner.next()));
        System.out.println("na-unifiedid:");
        webdriver.manage().addCookie(new Cookie("na-unifiedid", scanner.next()));
        System.out.println("scr_session:");
        webdriver.manage().addCookie(new Cookie("scr_session", scanner.next()));

        scanner.close();

        //endregion

        // loop
        while(programIterations >= 1)
        {
            //region scroll page down until the end
            javascriptExecutor = (JavascriptExecutor) webdriver;
            random = new Random();

            webdriver.get("https://scrap.tf/raffles/ending");

            boolean isScrolledToBottom = false;
            do
            {
                isScrolledToBottom = Boolean.parseBoolean(javascriptExecutor.executeScript("return ((window.innerHeight + window.pageYOffset) >= document.body.offsetHeight)").toString());

                javascriptExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(4000 + random.nextInt(1500));
            }
            while(isScrolledToBottom==false);

            //endregion

            //region parse web browser page

            parsedWebPage = Jsoup.parse(webdriver.getPageSource());
            raffleDivs = parsedWebPage.getElementsByAttributeValue("class", "panel-raffle");

            //endregion

            //region each handled <div> gives link to raffles list

            ArrayList<Raffle> rafflesList = new ArrayList<Raffle>();

            for(int i=0;i<raffleDivs.size();i++)
            {
                Element panelHeading = raffleDivs.get(i).child(0);
                Element raffleName = panelHeading.child(0);
                Element raffleNameLink = raffleName.child(0);
                String _url = "https://scrap.tf" + raffleNameLink.attr("href");
                String _title = raffleNameLink.text();
                rafflesList.add(new Raffle(_url, _title));
            }

            //endregion

            // region show collected raffles from raffles list

            System.out.println("collected raffles links:");
            for(int i=0; i<rafflesList.size(); i++)
            {
                System.out.println(rafflesList.get(i).toString());
            }

            //endregion

            //region enter raffles if possible

            if(rafflesList.size()>0)
            {
                for (int i=0; i<rafflesList.size(); i++)
                {
                    webdriver.get(rafflesList.get(i).url);
                    Thread.sleep(2500 + random.nextInt(3000));
                    parsedWebPage = Jsoup.parse(webdriver.getPageSource());

                    Elements joinButtons = parsedWebPage.getElementsByAttributeValue("class", "btn btn-embossed btn-info btn-lg");
                    try{ javascriptExecutor.executeScript(joinButtons.get(0).attr("onclick")); }
                    catch (Exception exception) { System.out.println(exception.getMessage()); }
                    Thread.sleep(2500 + random.nextInt(3000));
                }
            }

            //endregion

            programIterations--;

        }

        webdriver.quit();
    }
}

class Raffle
{
    public String url = "";
    public String name = "";

    public Raffle(String url, String name)
    {
        this.url = url;
        this.name = name;
    }

    @Override
    public String toString() {return url+" ------- "+name;}
}