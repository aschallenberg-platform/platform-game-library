package de.aschallenberg.gamelibrary.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameData implements Serializable {
	private String name;
	private String module;
	private String version;
	private HashMap<String, Object> settings;
	private List<BotData> bots;
}
