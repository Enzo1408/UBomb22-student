package fr.ubx.poo.ubomb.game;

public record Position (int x, int y) {

    public Position(Position position) {
        this(position.x, position.y);
    }

    @Override
    public boolean equals(Object obj) {
        Position pos = (Position)obj;

        return this.x == pos.x && this.y == pos.y;
    }
}
