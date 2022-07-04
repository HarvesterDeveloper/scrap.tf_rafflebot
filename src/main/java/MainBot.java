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
        // all local vars
        System.setProperty("webdriver.chrome.driver", "selenium\\chromedriver.exe");
        WebDriver webdriver = new ChromeDriver();
        HashMap<String, String> cookies_map = new HashMap<String, String>();
        ArrayList<Raffle> raffles_list = new ArrayList<>();
		Scanner scanner = new Scanner(System.in);
		JavascriptExecutor js_executor = (JavascriptExecutor) webdriver;
		Random random = new Random();

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

        // scroll 2 times down
        webdriver.get("https://scrap.tf/raffles/ending");
        js_executor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        Thread.sleep(4000+random.nextInt(1500));
        js_executor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        Thread.sleep(4000+random.nextInt(1500));

        // parse web browser page
        Document doc = Jsoup.parse(webdriver.getPageSource());
        Elements raffles = doc.getElementsByAttributeValue("class", "panel-raffle");

        // each handled <div> gives link to raffle list
        raffles.forEach(divElement ->
        {
            Element panelHeading = divElement.child(0);
            Element raffleName = panelHeading.child(0);
            Element raffleNameLink = raffleName.child(0);
            String _url = "https://scrap.tf"+raffleNameLink.attr("href");
            String _title = raffleNameLink.text();
            raffles_list.add(new Raffle(_url, _title));
        });

        // show collected raffles from raffles list
        System.out.println("ready to join raffles:");
        raffles_list.forEach(System.out::println);
        

        // enter raffles if possible
        if(raffles_list.size()>0)
        {
            for (int i=0; i<raffles_list.size();i++)
            {
                webdriver.get(raffles_list.get(i).url);
                doc = Jsoup.parse(webdriver.getPageSource());
                Elements joinButtons = doc.getElementsByAttributeValue("class", "btn btn-embossed btn-info btn-lg");
                System.out.println("Going execute : "+joinButtons.get(0).attr("onclick"));
                js_executor.executeScript(joinButtons.get(0).attr("onclick"));
                Thread.sleep(4000+random.nextInt(1500));
            }
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