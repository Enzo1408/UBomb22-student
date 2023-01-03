package fr.ubx.poo.ubomb.game;

import fr.ubx.poo.ubomb.go.GameObject;
import fr.ubx.poo.ubomb.go.character.Player;
import fr.ubx.poo.ubomb.go.decor.Decor;
import fr.ubx.poo.ubomb.go.decor.DoorNextClosed;
import fr.ubx.poo.ubomb.go.decor.DoorNextOpened;
import fr.ubx.poo.ubomb.go.decor.DoorPrevOpened;
import fr.ubx.poo.ubomb.launcher.Entity;

import java.util.ArrayList;

public class Game {

    private final Configuration configuration;
    private final Player player;

    private final Grid[] grids;
    private final int levels;
    public int activeLevel;
    public int oldLevel;
    public boolean levelChangeRequested = false;

    public ArrayList<GameObject> newRenderTargets;

    public Game(Configuration configuration, Grid[] grids)
    {
        this.configuration = configuration;

        this.grids = grids;
        this.levels = grids.length;
        this.activeLevel = 1;
        this.oldLevel = 1;

        player = new Player(this, configuration.playerPosition());

        newRenderTargets = new ArrayList<>();
    }

    public Configuration configuration() {
        return configuration;
    }


    public Grid grid() {
        return grids[activeLevel-1];
    }

    public Grid grid(int index) {
        return grids[index-1];
    }

    public DoorNextOpened findNextDoor()
    {
        for (Decor decor : grids[activeLevel-1].values())
        {
            if (decor instanceof DoorNextOpened)
            {
                return (DoorNextOpened) decor;
            }
        }

        return null;
    }

    public DoorPrevOpened findPreviousDoor()
    {
        for (Decor decor : grids[activeLevel-1].values())
        {
            if (decor instanceof DoorPrevOpened)
            {
                return (DoorPrevOpened) decor;
            }
        }

        return null;
    }

    public Player player() {
        return this.player;
    }

    public void addNewRenderTarget(GameObject go) { newRenderTargets.add(go);}

}
