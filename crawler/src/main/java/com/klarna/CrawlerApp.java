package com.klarna;

import java.util.*;
import java.net.*;
import java.io.*;

import com.klarna.Crawler;
import com.klarna.Sys;

public class CrawlerApp {
    public static void main (String[] args) throws Exception {
        
        if(args.length != 1) {
            Sys.out("Usage: java Crawler [URL/to/download]");
            return;
        }
        Crawler cr = new Crawler(args[0]);
        
        cr.logStats();
    }
}