package com.klarna;

import java.util.*;
import java.io.*;


class Sys {
    public static void out(String s) {
        System.out.println(s);
    }
    public static void err(String s) {
        System.err.println(s);
    }
    public static Properties readProp(ClassLoader l, String propertyFile)  throws IOException, FileNotFoundException {
        Properties prop = new Properties();
        InputStream inputStream = l.getResourceAsStream(propertyFile);

		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propertyFile + "' not found in the classpath");
		}
		return prop;
    }

}


