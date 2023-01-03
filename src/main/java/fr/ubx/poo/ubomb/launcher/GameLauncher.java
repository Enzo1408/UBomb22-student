package fr.ubx.poo.ubomb.launcher;

import fr.ubx.poo.ubomb.game.Configuration;
import fr.ubx.poo.ubomb.game.Game;
import fr.ubx.poo.ubomb.game.Level;
import fr.ubx.poo.ubomb.game.Position;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class GameLauncher {

    public static Game load()
    {
        Properties properties = new Properties();
        Reader in = null;
        try
        {
            in = new FileReader("world/sample.properties");
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Lol");
        }

        properties.load(in);

        Position player = properties.positionProperty("player", new Position(0, 0));

        int bombBagCapacity = properties.integerProperty("bombBagCapacity", 3);
        int playerLives = properties.integerProperty("playerLives", 5);
        int playerInvincibilityTime = properties.integerProperty("playerInvincibilityTime", 4000);
        int monsterVelocity = properties.integerProperty("monsterVelocity", 5);
        int monsterInvincibilityTime = properties.integerProperty("monsterInvincibilityTime", 1000);

        boolean compression = properties.booleanProperty("compression", false);
        int nbLevels = properties.integerProperty("levels", 1);

        Level[] levels = new Level[nbLevels];
        MapRepo mapLoader;

        for (int i = 0; i < nbLevels; i++)
        {
            String level = properties.stringProperty("level" + (i+1), "");

            if (compression)
            {
                mapLoader = new MapRepoStringRLE();
            }
            else
            {
                mapLoader = new MapRepoString();
            }

            MapLevel map;
            if (level.equals(""))
            {
                levels[i] = new Level(new MapLevelDefault());
            }
            else
            {
                levels[i] = new Level(mapLoader.load(level));
            }
        }




        Configuration configuration = new Configuration(player, bombBagCapacity, playerLives, playerInvincibilityTime, monsterVelocity, monsterInvincibilityTime);
        return new Game(configuration, levels);
    }

}
