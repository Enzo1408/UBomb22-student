package fr.ubx.poo.ubomb.game;

import fr.ubx.poo.ubomb.go.decor.bonus.*;
import fr.ubx.poo.ubomb.go.decor.*;
import fr.ubx.poo.ubomb.launcher.Entity;
import fr.ubx.poo.ubomb.launcher.MapLevel;

import java.util.*;

public class Level implements Grid {

    private final int width;

    private final int height;

    private final MapLevel entities;


    private final Map<Position, Decor> elements = new HashMap<>();

    public Level(MapLevel entities) {
        this.entities = entities;
        this.width = entities.width();
        this.height = entities.height();

        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                Position position = new Position(i, j);
                Entity entity = entities.get(i, j);
                switch (entity) {
                    case Stone:
                        elements.put(position, new Stone(position));
                        break;

                    case Tree:
                        elements.put(position, new Tree(position));
                        break;

                    case Princess:
                        elements.put(position, new Princess(position));
                        break;

                    case Monster:
                        elements.put(position, new Monster(position));
                        break;

                    case Box:
                        elements.put(position, new Box(position));
                        break;

                    //Bonuses
                    case Key:
                        elements.put(position, new Key(position));
                        break;

                    case BombRangeInc:
                        elements.put(position, new BombRangeInc(position));
                        break;
                    case BombRangeDec:
                        elements.put(position, new BombRangeDec(position));
                        break;

                    case BombNumberInc:
                        elements.put(position, new BombNumberInc(position));
                        break;
                    case BombNumberDec:
                        elements.put(position, new BombNumberDec(position));
                        break;

                    case Heart:
                        elements.put(position, new Heart(position));
                        break;

                    //Doors
                    case DoorNextClosed:
                        elements.put(position, new DoorNextClosed(position));
                        break;

                    case DoorNextOpened:
                        elements.put(position, new DoorNextOpened(position));
                        break;

                    case DoorPrevOpened:
                        elements.put(position, new DoorPrevOpened(position));
                        break;

                    case Empty: break;

                    default:
                        throw new RuntimeException("EntityCode " + entity.name() + " not processed");
                }
            }
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return this.height;
    }

    public Decor get(Position position) {
        return elements.get(position);
    }

    @Override
    public void remove(Position position) {
        elements.remove(position);
    }

    public Collection<Decor> values() {
        return elements.values();
    }


    @Override
    public boolean inside(Position position) {
        return (position.x() >= 0 && position.x() < width() &&
                position.y() >= 0 & position.y() < height());
    }

    @Override
    public void set(Position position, Decor decor) {
        if (!inside(position))
            throw new IllegalArgumentException("Illegal Position: (" + position.x() + ", " + position.y() + ")");
        if (decor != null)
            elements.put(position, decor);
    }


}
