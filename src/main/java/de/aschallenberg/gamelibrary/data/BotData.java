package de.aschallenberg.gamelibrary.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BotData implements Serializable {

	@EqualsAndHashCode.Include
	private UUID token;
	private String name;
	private String ownerName;
}
