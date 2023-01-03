package fr.ubx.poo.ubomb.launcher;

public class MapRepoString implements MapRepo
{

    @Override
    public MapLevel load(String string)
    {
        String[] lines = string.split("x");

        MapLevel level = new MapLevel(lines[0].length(), lines.length);

        for (int i = 0; i < lines.length; i++)
        {
            for (int j = 0; j < lines[i].length(); j++)
            {
                level.set(j, i, Entity.fromCode(lines[i].charAt(j)));
            }
        }

        return level;
    }

    @Override
    public String export(MapLevel mapLevel) {
        return null;
    }
}
