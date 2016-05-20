package aec;

import java.util.HashMap;
import org.apache.log4j.Logger;

public class Memory {
	
	static Logger logger = Logger.getLogger(Memory.class.getName());

	private static HashMap<Integer, String> memory = new HashMap<Integer, String>();
	
	public static void put(Integer key, String value) {
		memory.put(key, value);
		logger.info(key + " -> " + value);
	}
	
	public static String get(Integer key) {
		return memory.get(key);
	}
	
	public static String delete(Integer key) {
		return memory.remove(key);
	}
	
}
