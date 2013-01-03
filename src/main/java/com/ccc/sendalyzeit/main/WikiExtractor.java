package com.ccc.sendalyzeit.main;

import java.util.Arrays;
import java.util.Collection;

public class WikiExtractor {

	public final static Collection<String> DISCARD_ELEMENTS = Arrays.asList(    
			"gallery", "timeline", "noinclude", "pre",
	        "table", "tr", "td", "th", "caption",
	        "form", "input", "select", "option", "textarea",
	        "ul", "li", "ol", "dl", "dt", "dd", "menu", "dir",
	        "ref", "references", "img", "imagemap", "source"
	);
	
	
	public static String extractText(String input) {
		return input;
	}
	
}
