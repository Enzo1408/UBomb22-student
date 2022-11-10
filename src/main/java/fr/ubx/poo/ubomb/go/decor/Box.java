/*
 * Copyright (c) 2020. Laurent Réveillère
 */

package fr.ubx.poo.ubomb.go.decor;
import fr.ubx.poo.ubomb.game.Position;
import fr.ubx.poo.ubomb.go.Walkable;
import fr.ubx.poo.ubomb.go.character.Player;

public class Box extends Decor implements Walkable {
    public Box(Position position) {
        super(position);
    }

    @Override
    public boolean walkableBy(Player player) {
        return false;
    }
}