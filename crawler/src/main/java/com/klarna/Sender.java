package com.klarna;

import java.net.*;
import java.io.*;
import java.util.logging.*;



public class Sender {
    private static Logger log = Logger.getLogger(Sender.class.getName());

    
    URL createApiUrl;
    URL appendApiUrl;
    
    Sender(String api, String dst, String userName) throws Exception {
        dst = dst.replaceAll("^/","");
        
        String createApiUrlRaw = api + dst + "?op=CREATE" + "&user.name=" + userName + "&data=true";
        log.info("Got create api url for sender: " + createApiUrlRaw);
        createApiUrl = new URL(createApiUrlRaw);
        
        String appendApiUrlRaw = api + dst + "?op=APPEND" + "&user.name=" + userName + "&data=true";
        log.info("Got append api url for sender: " + appendApiUrlRaw);
        appendApiUrl = new URL(appendApiUrlRaw);
        
        precreate();
    }
    
    static void htmlReq(URL url, String method, String data) throws Exception {
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestMethod(method);
        httpCon.setRequestProperty("Content-Type", "application/octet-stream");
        OutputStreamWriter out = new OutputStreamWriter(
            httpCon.getOutputStream()
        );
        out.write(data);
        out.close();
        String response = Util.getStringFromInputStream(httpCon.getInputStream());
        log.info("Got response: ");
        log.info(response);
    }
    public void precreate() throws Exception {
        htmlReq(createApiUrl, "PUT", "");
    }
    
    public void sendHtml(String url, String data) throws Exception {
        htmlReq(appendApiUrl, "POST", url + " " + data);
    }
    
}