import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.*;

public class MainBot
{
    public static void main(String[] args) throws InterruptedException
    {
        //region vars declaration

        WebDriver webdriver = null;
        Scanner scanner = null;
        JavascriptExecutor javascriptExecutor = null;
        Random random = null;
        Document parsedWebPage = null;
        Elements raffleDivs = null;
        int programIterations = 1;

        //endregion

        // program iterations set
        System.out.println("Enter number of program iterations(how many times you need to start search and join raffles again)");
        scanner = new Scanner(System.in);
        programIterations = scanner.nextInt();

        // chrome webdriver settings
        System.setProperty("webdriver.chrome.driver", "selenium\\chromedriver.exe");

        try{ webdriver = new ChromeDriver(); }
        catch (Exception exception)
        {
            System.out.println(exception.getMessage());
            System.exit(0);
        }

        // cookie input
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

        // loop
        while(programIterations >= 1)
        {
            // scroll page down until the end
            javascriptExecutor = (JavascriptExecutor) webdriver;
            random = new Random();

            webdriver.get("https://scrap.tf/raffles/ending");

            boolean isScrolledToBottom = false;
            do
            {
                isScrolledToBottom = (boolean) javascriptExecutor.executeScript("return ((window.innerHeight + window.pageYOffset) >= document.body.offsetHeight)");

                javascriptExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(4000 + random.nextInt(1500));
            }
            while(isScrolledToBottom==false);

            // parse web browser page
            parsedWebPage = Jsoup.parse(webdriver.getPageSource());
            raffleDivs = parsedWebPage.getElementsByAttributeValue("class", "panel-raffle");

            // each handled <div> gives link to raffles list
            ArrayList<Raffle> rafflesList = new ArrayList<>();

            raffleDivs.forEach(divElement ->
            {
                Element panelHeading = divElement.child(0);
                Element raffleName = panelHeading.child(0);
                Element raffleNameLink = raffleName.child(0);
                String _url = "https://scrap.tf" + raffleNameLink.attr("href");
                String _title = raffleNameLink.text();
                rafflesList.add(new Raffle(_url, _title));
            });

            // show collected raffles from raffles list
            System.out.println("collected raffles links:");
            rafflesList.forEach(System.out::println);

            // enter raffles if possible
            if(rafflesList.size()>0)
            {
                for (int i=0; i<rafflesList.size(); i++)
                {
                    webdriver.get(rafflesList.get(i).url);
                    parsedWebPage = Jsoup.parse(webdriver.getPageSource());

                    Elements joinButtons = parsedWebPage.getElementsByAttributeValue("class", "btn btn-embossed btn-info btn-lg");

                    try{ javascriptExecutor.executeScript(joinButtons.get(0).attr("onclick")); }
                    catch (Exception exception) { System.out.println(exception.getMessage()); }
                    Thread.sleep(2500 + random.nextInt(3000));
                }
            }

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