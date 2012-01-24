package edu.mcmaster.maplelab.av.media;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParamTest {
	private static String[] _paramList = new String[]{"audioDurations", "notes", "bob", "shmitty"};
	public static Map<String, List<String>> _paramVals = new HashMap<String, List<String>>() {{
		put("audioDurations", new ArrayList<String>(){{add("N"); add("L"); add("S"); }});
		put("notes", new ArrayList<String>(){{add("D"); add("E"); add("F"); }});
		put("bob", new ArrayList<String>(){{add("A"); add("B"); add("C"); }});
		put("shmitty", new ArrayList<String>(){{add("do"); add("ray"); add("mi"); add("fa"); }});
	}};
	
	
	public static void test() {
		
		String format = "${audioDurations}${notes}_${bob}*${shmitty}";
		List<String> working = new ArrayList<String>();
		working.add(format);

		List<String> names = null;
		for (String param : _paramList) {
			String replace = "${" + param + "}";
			names = new ArrayList<String>();
			List<String> actualParams = _paramVals.get(param);
			for (String existing : working) {
				for (String item : actualParams) {
					names.add(existing.replace(replace, item));
				}
			}
			working = names;
		}
		
		System.out.println(names);
		System.out.println(names.size());
	}
	
	public static void main(String[] args) {
		ParamTest pt = new ParamTest();
		ParamTest.test();
		
		System.out.println("ah");
	}
}
