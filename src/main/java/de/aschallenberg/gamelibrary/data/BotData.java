package de.aschallenberg.gamelibrary.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BotData implements Serializable {

	@EqualsAndHashCode.Include
	private String name;
	private String ownerName;
}
