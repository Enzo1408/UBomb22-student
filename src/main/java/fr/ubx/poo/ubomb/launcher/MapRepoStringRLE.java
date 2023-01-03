package fr.ubx.poo.ubomb.launcher;

public class MapRepoStringRLE extends MapRepoString
{
    @Override
    public MapLevel load(String string)
    {
        String[] lines = string.split("x");

        StringBuilder nonCompressedLevel = new StringBuilder();

        //Here we assume that, in RLE compression, numbers do not exceed 9
        for (String line : lines)
        {
            for (int i = 0; i < line.length(); i++)
            {
                if (i < (line.length() - 1) && isNumber(line.charAt(i+1)))
                {
                    int repeat = Integer.parseInt(String.valueOf(line.charAt(i+1)));

                    for (int j = 0; j < repeat; j++) //Unravelling elements, B4 -> BBBB
                    {
                        nonCompressedLevel.append(line.charAt(i));
                    }

                    i++;
                }

                else
                {
                    nonCompressedLevel.append(line.charAt(i));
                }
            }

            nonCompressedLevel.append("x");
        }

        return super.load(nonCompressedLevel.toString());
    }

    @Override
    public String export(MapLevel mapLevel)
    {
        return null;
    }

    public boolean isNumber(char target)
    {
        try
        {
            Integer.parseInt(String.valueOf(target));
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }
}
