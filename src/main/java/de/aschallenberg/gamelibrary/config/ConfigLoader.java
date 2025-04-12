package de.aschallenberg.gamelibrary.config;

import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Log4j2
public class ConfigLoader {
	private static final String CONFIG_FILE = "config.properties";
	private static final Properties PROPERTIES = new Properties();

	private static final Map<String, String> ARG_TO_PROPERTY_KEY = new HashMap<>();

	static {
		ARG_TO_PROPERTY_KEY.put("host", "platform.host");
		ARG_TO_PROPERTY_KEY.put("port", "platform.port");
		ARG_TO_PROPERTY_KEY.put("ssl", "platform.ssl");
		ARG_TO_PROPERTY_KEY.put("token", "platform.game.token");
	}

	private ConfigLoader() {}

	public static void load(String[] args) {
		try {
			PROPERTIES.load(ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE));
			log.info("Loaded configuration from {}", CONFIG_FILE);
		} catch (Exception e) {
			log.error("Missing {} file. Please read the README.md!", CONFIG_FILE);
		}

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			if (arg.startsWith("--")) {
				if (args.length == i + 1) {
					log.warn("Missing value for arg {}. Using the value of the config.properties", arg);
					break;
				}

				String argKey = arg.substring(2);
				if (!ARG_TO_PROPERTY_KEY.containsKey(argKey)) {
					log.warn("Unknown arg {}", arg);
					continue;
				}

				String value = args[i + 1];
				String propertyKey = ARG_TO_PROPERTY_KEY.get(argKey);

				log.info("Overriding property {} to {}", propertyKey, value);

				PROPERTIES.setProperty(ARG_TO_PROPERTY_KEY.get(argKey), args[i + 1]);
			}
		}
	}

	public static String get(String key) {
		return PROPERTIES.getProperty(key);
	}
}
