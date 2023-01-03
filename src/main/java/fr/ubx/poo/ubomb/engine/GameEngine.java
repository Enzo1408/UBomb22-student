/*
 * Copyright (c) 2020. Laurent Réveillère
 */

package fr.ubx.poo.ubomb.engine;

import fr.ubx.poo.ubomb.game.Direction;
import fr.ubx.poo.ubomb.game.Game;
import fr.ubx.poo.ubomb.game.Position;
import fr.ubx.poo.ubomb.go.GameObject;
import fr.ubx.poo.ubomb.go.character.Player;
import fr.ubx.poo.ubomb.go.decor.*;
import fr.ubx.poo.ubomb.go.decor.bonus.Bomb;
import fr.ubx.poo.ubomb.go.decor.bonus.Bonus;
import fr.ubx.poo.ubomb.go.decor.bonus.Explosion;
import fr.ubx.poo.ubomb.go.decor.bonus.Key;
import fr.ubx.poo.ubomb.view.ImageResource;
import fr.ubx.poo.ubomb.view.Sprite;
import fr.ubx.poo.ubomb.view.SpriteFactory;
import fr.ubx.poo.ubomb.view.SpritePlayer;
import javafx.animation.AnimationTimer;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import java.util.Timer;

import static fr.ubx.poo.ubomb.view.ImageResource.*;


public final class GameEngine {

    private static AnimationTimer gameLoop;
    private final Game game;
    private final Player player;
    private final List<Sprite> sprites = new LinkedList<>();
    private final Set<Sprite> cleanUpSprites = new HashSet<>();
    private final Stage stage;
    private StatusBar statusBar;
    private Pane layer;
    private Input input;

    private long lastMonsterUpdate = 0;

    private List<Bomb> bombs = new LinkedList<>();
    private List<Monster> monsters = new LinkedList<>();

    public GameEngine(Game game, final Stage stage) {
        this.stage = stage;
        this.game = game;
        this.player = game.player();
        initialize();
        buildAndSetGameLoop();
    }

    public void cleanInit()
    {
        sprites.forEach(Sprite::remove);
        sprites.clear();

        for (Monster monster : monsters)
        {
            game.grid(game.oldLevel).set(monster.getPosition(), monster);
        }

        monsters.clear();
        bombs.clear();

        // Create sprites
        for (var decor : game.grid().values()) {
            if (decor instanceof Monster monster)
            {
                monsters.add(monster);
            }
            sprites.add(SpriteFactory.create(layer, decor));
            decor.setModified(true);
        }

        GameObject door = null;
        if (game.activeLevel < game.oldLevel) // DoorPrev
        {
            door = game.findNextDoor();
        }
        else if (game.activeLevel > game.oldLevel) //Door next
        {
            door = game.findPreviousDoor();
        }

        if (door != null)
        {
            player.setPosition(door.getPosition());
        }
        else
        {
            player.setPosition(new Position(0, 0));
        }

        sprites.add(new SpritePlayer(layer, player));

        stage.sizeToScene();
    }

    private void initialize() {
        Group root = new Group();
        layer = new Pane();

        int height = game.grid().height();
        int width = game.grid().width();
        int sceneWidth = width * ImageResource.size;
        int sceneHeight = height * ImageResource.size;
        Scene scene = new Scene(root, sceneWidth, sceneHeight + StatusBar.height);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());

        stage.setScene(scene);
        stage.setResizable(true);
        stage.sizeToScene();
        stage.hide();
        stage.show();

        input = new Input(scene);
        root.getChildren().add(layer);
        statusBar = new StatusBar(root, sceneWidth, sceneHeight, game);

        // Create sprites
        for (var decor : game.grid().values())
        {
            if (decor instanceof Monster monster)
            {
                monsters.add(monster);
            }

            sprites.add(SpriteFactory.create(layer, decor));
            decor.setModified(true);
        }

        sprites.add(new SpritePlayer(layer, player));

    }

    void buildAndSetGameLoop() {
        gameLoop = new AnimationTimer() {
            public void handle(long now) {
                // Check keyboard actions
                processInput(now);

                // Do actions
                update(now);
                createNewBombs(now);
                checkCollision(now);
                checkExplosions(now);

                // Graphic update
                cleanupSprites();
                render();
                statusBar.update(game);
            }
        };
    }

    private void checkExplosions(long now)
    {
        for (Bomb bomb : bombs)
        {
            long timeSinceBirth = (now - bomb.birth());

            if (timeSinceBirth > 1_000_000_000L && timeSinceBirth < 2_000_000_000L) //>1 second (first stage)
            {
                bomb.sprite.remove();
                sprites.remove(bomb.sprite);

                bomb.sprite = new Sprite(layer, BOMB_2.getImage(), bomb);
                sprites.add(bomb.sprite);
                bomb.setModified(true);
            }

            else if (timeSinceBirth > 2_000_000_000L && timeSinceBirth < 3_000_000_000L) //>2 seconds (second stage)
            {
                bomb.sprite.remove();
                sprites.remove(bomb.sprite);

                bomb.sprite = new Sprite(layer, BOMB_1.getImage(), bomb);
                sprites.add(bomb.sprite);
                bomb.setModified(true);
            }

            else if (timeSinceBirth > 3_000_000_000L && timeSinceBirth < 4_000_000_000L) //>3 seconds (third stage)
            {
                bomb.sprite.remove();
                sprites.remove(bomb.sprite);

                bomb.sprite = new Sprite(layer, BOMB_0.getImage(), bomb);
                sprites.add(bomb.sprite);
                bomb.setModified(true);
            }

            else if (timeSinceBirth > 4_000_000_000L) //>4 seconds (explosion)
            {
                bomb.sprite = null;
                bomb.remove();
                bombs.remove(bomb);
                sprites.remove(bomb.sprite);

                int range = player.getBombRange();
                ArrayList<Explosion> gos = new ArrayList<>();
                ArrayList<Sprite> explosions = new ArrayList<>();

                boolean upBlocked = false;
                boolean downBlocked = false;
                boolean leftBlocked = false;
                boolean rightBlocked = false;
                for (int i = 1; i < range + 1; i++)
                {
                    Position bombPos = bomb.getPosition();
                    //Bomb pos
                    if (shouldExplode(bombPos))
                    {
                        Explosion bombExplosion = new Explosion(bombPos);
                        gos.add(bombExplosion);
                        explosions.add(new Sprite(layer, EXPLOSION.getImage(), bombExplosion));

                        Decor decor = game.grid().get(bombPos);
                        if (decor != null) { decor.remove();}
                        if (decor instanceof Monster monster) { monsters.remove(monster); monster.remove();}

                        if (playerHitByBomb(bombPos))
                        {
                            player.setLives(player.getLives() - 1);
                        }
                    }

                    //UP
                    Position upPos = new Position(bombPos.x(), bombPos.y()-i);
                    if (!upBlocked && shouldExplode(upPos))
                    {
                        Explosion upExplosion = new Explosion(upPos);
                        gos.add(upExplosion);
                        explosions.add(new Sprite(layer, EXPLOSION.getImage(), upExplosion));

                        Decor decor = game.grid().get(upPos);
                        if (decor != null) { decor.remove();}
                        if (decor instanceof Monster monster) { monsters.remove(monster); monster.remove();}

                        if (blocksExplosion(upPos))
                        {
                            upBlocked = true;
                        }

                        if (playerHitByBomb(upPos))
                        {
                            player.setLives(player.getLives() - 1);
                        }
                    }


                    //DOWN
                    Position downPos = new Position(bombPos.x(), bombPos.y()+i);
                    if (!downBlocked && shouldExplode(downPos))
                    {
                        Explosion downExplosion = new Explosion(downPos);
                        gos.add(downExplosion);
                        explosions.add(new Sprite(layer, EXPLOSION.getImage(), downExplosion));

                        Decor decor = game.grid().get(downPos);
                        if (decor != null) { decor.remove();}
                        if (decor instanceof Monster monster) { monsters.remove(monster); monster.remove();}

                        if (blocksExplosion(downPos))
                        {
                            downBlocked = true;
                        }

                        if (playerHitByBomb(downPos))
                        {
                            player.setLives(player.getLives() - 1);
                        }
                    }

                    //LEFT
                    Position leftPos = new Position(bombPos.x()-i, bombPos.y());
                    if (!leftBlocked && shouldExplode(leftPos))
                    {
                        Explosion leftExplosion = new Explosion(leftPos);
                        gos.add(leftExplosion);
                        explosions.add(new Sprite(layer, EXPLOSION.getImage(), leftExplosion));

                        Decor decor = game.grid().get(leftPos);
                        if (decor != null) { decor.remove();}
                        if (decor instanceof Monster monster) { monsters.remove(monster); monster.remove();}

                        if (blocksExplosion(leftPos))
                        {
                            leftBlocked = true;
                        }

                        if (playerHitByBomb(leftPos))
                        {
                            player.setLives(player.getLives() - 1);
                        }
                    }

                    //RIGHT
                    Position rightPos = new Position(bombPos.x()+i, bombPos.y());
                    if (!rightBlocked && shouldExplode(rightPos))
                    {
                        Explosion rightExplosion = new Explosion(rightPos);
                        gos.add(rightExplosion);
                        explosions.add(new Sprite(layer, EXPLOSION.getImage(), rightExplosion));

                        Decor decor = game.grid().get(rightPos);
                        if (decor != null) { decor.remove();}
                        if (decor instanceof Monster monster) { monsters.remove(monster); monster.remove();}

                        if (blocksExplosion(rightPos))
                        {
                            rightBlocked = true;
                        }

                        if (playerHitByBomb(rightPos))
                        {
                            player.setLives(player.getLives() - 1);
                        }
                    }
                }

                for (Sprite explosion : explosions)
                {
                    sprites.add(explosion);
                }

                Timer timer = new Timer();

                TimerTask task = new TimerTask() {
                    @Override
                    public void run()
                    {
                        for (Explosion go : gos)
                        {
                            go.remove();
                        }

                        player.setBombs(player.getBombs() + 1);
                    }
                };

                timer.schedule(task, 1000L); //1 second later

            }


        }
    }

    private boolean playerHitByBomb(Position pos)
    {
        return player.getPosition().equals(pos);
    }

    private boolean shouldExplode(Position pos)
    {
        Decor decor = game.grid().get(pos);
        if (decor == null) { return true;}

        return isBox(decor) || ((decor instanceof Bonus) && !(decor instanceof Key)) || decor instanceof Monster;
    }

    private boolean blocksExplosion(Position pos)
    {
        Decor decor = game.grid().get(pos);
        if (decor == null) { return false;}

        return isBox(decor) || isTreeStone(decor) || !game.grid().inside(pos); //Restrict bombs from exploding outside of level
    }

    private boolean isBox(Decor decor)
    {
        return decor instanceof Box;
    }

    private boolean isTreeStone(Decor decor)
    {
        return (decor instanceof Tree) || (decor instanceof Stone);
    }

    private void animateExplosion(Position src, Position dst) {
        ImageView explosion = new ImageView(ImageResource.EXPLOSION.getImage());
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), explosion);
        tt.setFromX(src.x() * Sprite.size);
        tt.setFromY(src.y() * Sprite.size);
        tt.setToX(dst.x() * Sprite.size);
        tt.setToY(dst.y() * Sprite.size);
        tt.setOnFinished(e -> {
            layer.getChildren().remove(explosion);
        });
        layer.getChildren().add(explosion);
        tt.play();
    }

    private void createNewBombs(long now)
    {
        if (player.bombRequested && player.canBomb())
        {
            player.bombRequested = false;
            Bomb bomb = new Bomb(player.getPosition(), now);
            bomb.sprite = SpriteFactory.create(layer, bomb);
            bombs.add(bomb);
            sprites.add(bomb.sprite);
        }
    }

    private void checkCollision(long now) {
        // Check a collision between a monster and the player
    }

    private void processInput(long now) {
        if (input.isExit())
        {
            gameLoop.stop();
            Platform.exit();
            System.exit(0);
        }
        else if (input.isMoveDown())
        {
            player.requestMove(Direction.DOWN);
        }
        else if (input.isMoveLeft())
        {
            player.requestMove(Direction.LEFT);
        }
        else if (input.isMoveRight())
        {
            player.requestMove(Direction.RIGHT);
        }
        else if (input.isMoveUp())
        {
            player.requestMove(Direction.UP);
        }

        else if (input.isKey())
        {
            player.requestKeyUse(player.getDirection());
        }

        else if (input.isBomb())
        {
            player.requestBomb(player.getDirection());

        }
        input.clear();
    }

    private void showMessage(String msg, Color color) {
        Text waitingForKey = new Text(msg);
        waitingForKey.setTextAlignment(TextAlignment.CENTER);
        waitingForKey.setFont(new Font(60));
        waitingForKey.setFill(color);
        StackPane root = new StackPane();
        root.getChildren().add(waitingForKey);
        Scene scene = new Scene(root, 400, 200, Color.WHITE);
        stage.setScene(scene);
        input = new Input(scene);
        stage.show();
        new AnimationTimer() {
            public void handle(long now) {
                processInput(now);
            }
        }.start();
    }


    private void update(long now)
    {
        if (game.levelChangeRequested)
        {
            cleanInit();
            game.levelChangeRequested = false;
            return;
        }

        player.update(now);

        if (player.getLives() == 0) {
            gameLoop.stop();
            showMessage("Perdu!", Color.RED);
        }

        if (player.hasWon())
        {
            gameLoop.stop();
            showMessage("Gagné!", Color.GREEN);
        }

        if (monsters.size() <= 0 || (now - lastMonsterUpdate) < (10_000_000_000L / game.configuration().monsterVelocity() / monsters.size()))
        {
            return;
        }

        Random rand = new Random();
        int chosen = rand.nextInt(0, monsters.size());

        Direction direction = Direction.random();
        Position nextPos = direction.nextPosition(monsters.get(chosen).getPosition());

        if ((game.grid().get(nextPos) == null || game.grid().get(nextPos) instanceof Bonus) && game.grid().inside(nextPos)) {
            monsters.get(chosen).remove();
            monsters.remove(chosen);

            Monster newMonster = new Monster(nextPos);
            game.grid().set(newMonster.getPosition(), newMonster);
            monsters.add(newMonster);

            if (direction == Direction.UP) {
                sprites.add(new Sprite(layer, MONSTER_UP.getImage(), newMonster));
            } else if (direction == Direction.DOWN) {
                sprites.add(new Sprite(layer, MONSTER_DOWN.getImage(), newMonster));
            } else if (direction == Direction.LEFT) {
                sprites.add(new Sprite(layer, MONSTER_LEFT.getImage(), newMonster));
            } else if (direction == Direction.RIGHT) {
                sprites.add(new Sprite(layer, MONSTER_RIGHT.getImage(), newMonster));
            }

        }

        lastMonsterUpdate = now;

    }

    public void cleanupSprites() {
        sprites.forEach(sprite -> {
            if (sprite.getGameObject().isDeleted()) {
                game.grid().remove(sprite.getPosition());
                cleanUpSprites.add(sprite);
            }
        });
        cleanUpSprites.forEach(Sprite::remove);
        sprites.removeAll(cleanUpSprites);
        cleanUpSprites.clear();
    }

    private void render()
    {
        for (GameObject go : game.newRenderTargets)
        {
            sprites.add(SpriteFactory.create(layer, go));
        }

        game.newRenderTargets.clear();

        sprites.forEach(Sprite::render);
    }

    public void start() {
        gameLoop.start();
    }

}