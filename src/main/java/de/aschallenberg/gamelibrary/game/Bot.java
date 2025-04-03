package de.aschallenberg.gamelibrary.game;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Bot {

    @EqualsAndHashCode.Include
    private final UUID token;
    private final String name;
    private final String ownerName;

   public static Bot fromMap(Map<String, Object> map) {
        return new Bot(
                UUID.fromString((String) map.get("token")),
                (String) map.get("name"),
                (String) map.get("ownerName")
        );
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "token", token.toString(),
                "name", name,
                "ownerName", ownerName
        );
   }
}
