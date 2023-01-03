package fr.ubx.poo.ubomb.launcher;

import fr.ubx.poo.ubomb.game.Position;

import java.io.Reader;
import java.util.HashMap;
import java.util.Scanner;

public class Properties
{
    private HashMap<String, String> properties;

    public Properties()
    {
        properties = new HashMap<>();
    }

    public void load(Reader in)
    {
        Scanner scanner = new Scanner(in).useDelimiter("\n");

        while (scanner.hasNext())
        {
            String tmp = scanner.next();

            String[] groups = tmp.split("=");
            groups[0] = groups[0].trim();
            groups[1] = groups[1].replaceAll("\r", "").trim(); //Get rid of \r and whitespaces
            if (groups.length != 2)
            {
                System.out.println("wtf?");
                //throw new Exception("Malformed properties file");
            }

            properties.put(groups[0], groups[1]);
        }

    }

    public boolean booleanProperty(String name, boolean defaultValue)
    {
        if (!properties.containsKey(name))
        {
            return defaultValue;
        }

        return Boolean.parseBoolean(properties.get(name));
    }

    public int integerProperty(String name, int defaultValue)
    {
        if (!properties.containsKey(name))
        {
            return defaultValue;
        }

        return Integer.parseInt(properties.get(name));
    }

    public String stringProperty(String name, String defaultValue)
    {
        if (!properties.containsKey(name))
        {
            return defaultValue;
        }

        return properties.get(name);
    }

    public Position positionProperty(String name, Position defaultValue)
    {
        if (!properties.containsKey(name))
        {
            return defaultValue;
        }

        String[] xy = properties.get(name).split("x");

        return new Position(Integer.parseInt(xy[0]), Integer.parseInt(xy[1]));
    }
}
