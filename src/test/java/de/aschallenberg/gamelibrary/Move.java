package de.aschallenberg.gamelibrary;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Move implements Serializable {
	private int[] board;
	private int player;
}
