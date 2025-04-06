package de.aschallenberg.gamelibrary.config;

import lombok.extern.log4j.Log4j2;

import java.util.Properties;

@Log4j2
public class ConfigLoader {
	private static final String CONFIG_FILE = "config.properties";
	private static final Properties PROPERTIES = new Properties();

	private ConfigLoader() {}

	public static void load() {
		try {
			PROPERTIES.load(ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE));
			log.info("Loaded configuration from {}", CONFIG_FILE);
		} catch (Exception e) {
			log.error("Missing {} file. Please read the README.md!", CONFIG_FILE);
		}
	}

	public static String get(String key) {
		return PROPERTIES.getProperty(key);
	}
}
