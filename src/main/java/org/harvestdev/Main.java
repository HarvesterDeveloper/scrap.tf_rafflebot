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

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.*;
import java.util.*;

public class Main
{
    private static final Random random = new Random();

    public static void main(String[] args) throws IOException {

        // extract chromedriver.exe from jar

        FileOutputStream output = new FileOutputStream("chromedriver.exe");
        InputStream input;
        try {
            input = Main.class.getClassLoader().getResource("chromedriver.exe").openStream();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
            return;
        }

        byte [] buffer = new byte[4096];
        int bytesRead = input.read(buffer);

        while (bytesRead != -1) {
            output.write(buffer, 0, bytesRead);
            bytesRead = input.read(buffer);
        }

        output.close();
        input.close();

        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");

        // window setup

        JFrame frame = new JFrame("scrap.tf raffle bot");
        frame.setSize(512, 256);
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        JLabel label = new JLabel("scr_session");
        JTextField textField = new JTextField();
        textField.setColumns(30);
        JTextArea textArea = new JTextArea();
        textArea.setMargin(new Insets(8, 8, 8, 8));
        textArea.setLineWrap(true);
        JButton btnLaunch = new JButton("launch");
        btnLaunch.addActionListener(e -> new Thread(() -> {
            try {
                launchBot(textField.getText(), textArea);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }).start());
        JScrollPane scroll= new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        DefaultCaret caret = (DefaultCaret)textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        panel.add(label);
        panel.add(textField);
        panel.add(btnLaunch);

        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.getContentPane().add(BorderLayout.CENTER, scroll);
        frame.setVisible(true);
    }

    public static void launchBot(String sessionCookie, JTextArea out) throws InterruptedException {

        // chromedriver launch

        WebDriver webdriver;
        ChromeOptions options;

        try {
            options = new ChromeOptions();
            options.addArguments("--remote-allow-origins=*");
            webdriver = new ChromeDriver(options);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // cookie

        webdriver.get("https://scrap.tf/raffles/ending");
        webdriver.manage().deleteAllCookies();
        webdriver.manage().addCookie(new Cookie("scr_session", sessionCookie));

        // scrolling page until reaching bottom

        JavascriptExecutor jsExecutor = (JavascriptExecutor) webdriver;

        webdriver.get("https://scrap.tf/raffles/ending");

        boolean isScrolledToBottom;
        do {
            isScrolledToBottom = Boolean.parseBoolean(jsExecutor.executeScript(
                    "return ((window.innerHeight + window.pageYOffset) >= document.body.offsetHeight)")
                    .toString());

            jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(4000 + random.nextInt(1500));
        } while(!isScrolledToBottom);

        // parse

        Document parsedWebPage = Jsoup.parse(webdriver.getPageSource());
        Elements raffleDivs = parsedWebPage.getElementsByAttributeValue("class", "panel-raffle");

        // each handled <div> gives link to raffles list

        ArrayList<String> rafflesList = new ArrayList<>();

        for (Element raffleDiv : raffleDivs) {
            Element panelHeading = raffleDiv.child(0);
            Element raffleName = panelHeading.child(0);
            Element raffleNameLink = raffleName.child(0);
            String link = "https://scrap.tf" + raffleNameLink.attr("href");
            rafflesList.add(link);
        }

        // show collected raffles from raffles list

        for (String value : rafflesList) {
            out.setText(out.getText() + "\n" + value);
        }

        // enter raffles if possible

        if(rafflesList.size() > 0) {
            for (int i = 0; i < rafflesList.size(); i++) {
                String s = rafflesList.get(i);
                webdriver.get(s);
                Thread.sleep(2500 + random.nextInt(3000));
                parsedWebPage = Jsoup.parse(webdriver.getPageSource());

                Elements joinButtons = parsedWebPage.getElementsByAttributeValue("class", "btn btn-embossed btn-info btn-lg");
                try {
                    jsExecutor.executeScript(joinButtons.get(0).attr("onclick"));
                } catch (Exception exception) {
                    JOptionPane.showMessageDialog(null, exception.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
                }

                out.setText(out.getText() + "\n" + i + "/" + rafflesList.size());
                Thread.sleep(2500 + random.nextInt(3000));
            }
        }

        webdriver.quit();
    }
}