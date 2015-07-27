package com.klarna;

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.IllegalArgumentException;

import com.klarna.Crawler;

public class HistText {

    public static void main (String[] args) throws Exception {
        if(args.length == 0) {
            throw new IllegalArgumentException("Expecting arguments and it must be an urls");
        }
        Map<String, Integer> Hist = new LinkedHashMap<String, Integer>(15, 0.75f, true);
        for(String a: args) {
            String content = Crawler.getText(a);
            for(String w: content.split("[\\W]+")) {
                w = w.toLowerCase();
                int count = Hist.containsKey(w) ? Hist.get(w) : 0;
                Hist.put(w, count+1);
            }
        }
    
        Map<Integer, List<String>> sortedHist = new TreeMap<>();
        
        for (Map.Entry<String, Integer> entry : Hist.entrySet()) {
            Integer v = entry.getValue();
            List<String> l = sortedHist.containsKey(v) ? sortedHist.get(v) : new ArrayList<String>();
            l.add(entry.getKey());
            sortedHist.put(v, l);
        }
        for (Map.Entry<Integer, List<String>> entry : sortedHist.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        
        
        
    }
    
    

}