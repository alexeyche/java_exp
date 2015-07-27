package com.klarna;

import java.util.*;
import java.util.logging.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.*;
import java.util.regex.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import com.klarna.HumbleLogFormatter;


public class Crawler {
    
    
    private static Logger log = Logger.getLogger(Crawler.class.getName());
    
    CrawlStat stat;
    CrawlConfig conf;
    Sender send;
    TreeSet<String> downloadHistory;
    
    public static String getRawHtml(String url) throws Exception {
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(
            new InputStreamReader(
                connection.getInputStream()
            )
        );

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();

        return response.toString();
    }
    
    class CrawlConfig {
        private static final String configFile = "crawler.properties";
    
        CrawlConfig() throws IOException, FileNotFoundException {
            Properties prop = Sys.readProp(getClass().getClassLoader(), configFile);
            
    		sameHost = Boolean.parseBoolean(prop.getProperty("sameHost"));
    		downloadTimeout = Long.parseLong(prop.getProperty("downloadTimeout"));
    		sendApiUrl = prop.getProperty("sendApiUrl");
    		sendDst = prop.getProperty("sendDst");
    		sendUserName = prop.getProperty("sendUserName");
    		sendOn = Boolean.parseBoolean(prop.getProperty("sendOn"));
        }
        
        public boolean sameHost;
        public long downloadTimeout;
        public String sendApiUrl;
        public String sendDst;
        public String sendUserName;
        public boolean sendOn;
    }
    
    class CrawlStat {
        public int onPlate;
        public int eaten;
        public int digested;
        public int sent;
        
        @Override public String toString() {
            StringBuilder result = new StringBuilder();
            String NEW_LINE = System.getProperty("line.separator");
            result.append(this.getClass().getName() + " Object {" + NEW_LINE);
            result.append("\tonPlate: " + onPlate + NEW_LINE);
            result.append("\teaten: " + eaten + NEW_LINE);
            result.append("\tdigested: " + digested + NEW_LINE);
            result.append("\tsent: " + sent + NEW_LINE);
            result.append("}" + NEW_LINE);
            return result.toString();
        }
    }
    
    static Pattern hostPatt = Pattern.compile("(?:https*://(?:www.)*)*([^/]+)");
    
    static boolean havingSameHost(String host, String url) {
        if(!url.startsWith("http")) {
            return true; // relative url
        }
        
        Matcher mHost = hostPatt.matcher(host);
        if(!mHost.find()) return false;
        Matcher mUrl = hostPatt.matcher(url);
        if(!mUrl.find()) return false;
        
        String lHost = mHost.group(1);
        String rHost = mUrl.group(1);
        return rHost.endsWith(lHost) || lHost.endsWith(rHost);
    }
    
    static Pattern banPatt = Pattern.compile(".jar$");
    
    private boolean filterLink(String baseUrl, String hrefUrl) {
        boolean empty = hrefUrl.isEmpty();
        if (empty) {
            return true;
        }
        if(banPatt.matcher(hrefUrl).matches()) {
            log.info("Url is banned: " + hrefUrl);
        }
        
        
        if( !havingSameHost(baseUrl, hrefUrl) && conf.sameHost) {
            return true;
        }
        return false;
    }
    
    private static String normUrl(String url) {
        return url.replaceAll("^\\s+|\\s+$", "").replaceAll("^//", "http://").replaceAll("#.*", ""); 
    }
    
    private void eatUrl(String url, int recurseLevel) {
        if(recurseLevel == 5) { 
            return;
        }
        try {
            log.info("Waiting timeout ...");
        
            Thread.sleep(conf.downloadTimeout);
            
            log.info("Crawling " + url + " (recurseLevel: " + recurseLevel + ")" + " ... ");
            stat.onPlate++;
            String h = getRawHtml(url);
            stat.eaten++;
            downloadHistory.add(url);
            
            Document doc = Jsoup.parse(h);    
            Elements links = doc.getElementsByTag("a");
            log.info("Got links from " + url + " (" + links.size() + " num):");
            TreeSet<String> filtLinks = new TreeSet<String>();
            for (Element link : links) {
                String linkHref = normUrl(link.attr("href"));
                String linkText = link.text();
                if(linkHref.isEmpty()) {
                    continue;
                }
                boolean filtered = filterLink(url, linkHref);
                if(!filtered) {
                    filtLinks.add(linkHref);
                }
            }
            log.info("filtered " + (links.size() - filtLinks.size()));
            stat.digested++;
            
            if(conf.sendOn) {
                send.sendHtml(url, h);
            }
            stat.sent++;
            
            for(String s: filtLinks) {
                if(!s.startsWith("http")) {
                    String rels = new URL(new URL(url), s).toString();
                    log.info("Got relative: " + rels);
                    s = rels;
                }
                if(!downloadHistory.contains(s)) {
                    eatUrl(s, recurseLevel + 1);
                } else {
                    log.info(s + " was already downloaded through session, ignoring ... ");
                }
            }
            
        } catch (Exception e) {
            log.warning("Failed to process " + url + ": ");
            e.printStackTrace();
            log.warning(e.toString());
            log.warning("ignoring ...");
        }
    }
    
    public Crawler(String startUrl) throws Exception {
        log.setUseParentHandlers(false);
        
        HumbleLogFormatter formatter = new HumbleLogFormatter();
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        log.addHandler(handler);
        
        stat = new CrawlStat();
        conf = new CrawlConfig();
        send = new Sender(conf.sendApiUrl, conf.sendDst, conf.sendUserName);
        downloadHistory = new TreeSet<String>();
        
        eatUrl(startUrl, 0);
    }
    
    public void logStats() {
        log.info("Crawler stats =========================");
        for(String s: stat.toString().split("\n")) {
            log.info(s);
        }
        log.info("=======================================");
    }    
    public static String getText(String url) throws Exception {
        return getRawHtml(url).replaceAll("\\<.*?>","");
    }
}

