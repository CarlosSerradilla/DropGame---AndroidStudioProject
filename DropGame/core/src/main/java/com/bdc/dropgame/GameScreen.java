package com.bdc.dropgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameScreen implements Screen {

    Main game;
    Texture dropTexture;
    Texture dropBigTexture;
    Texture bombTexture;
    Texture backgroundTexture;
    Texture bucketTexture;
    Sound dropSound;
    Sound bigDropSound;
    Sound bombSound;
    Music backgroundMusic;
    FitViewport viewport;
    SpriteBatch spriteBatch;
    Sprite bucketSprite;
    Vector2 touchpos;
    BitmapFont font;

    Array<Sprite> droplets;
    float dropTimer;
    float bigDropTimer;
    float bombTimer;
    final float DROP_INTERVAL = 1f;
    final float BIG_DROP_INTERVAL = 5.5f;
    final float BOMB_INTERVAL = 3.2f;

    // Mundo 800x500 (igual que MenuScreen)
    final float WORLD_WIDTH  = 800f;
    final float WORLD_HEIGHT = 500f;

    // Tamaño del cubo y gotas en el nuevo mundo
    final float BUCKET_SIZE = 80f;
    final float DROP_SIZE   = 40f;
    final float BIG_DROP_SIZE   = 60f;
    final float BOMB_SIZE   = 70f;

    // Temporizador y puntaje
    float tiempoRestante = 60f;
    int puntaje = 0;
    boolean juegoTerminado = false;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        backgroundTexture = new Texture("background.png");
        dropTexture       = new Texture("drop.png");
        dropBigTexture    = new Texture("dropBig.png");
        bombTexture    = new Texture("bomb.png");
        bucketTexture     = new Texture("bucket.png");

        dropSound       = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        bigDropSound       = Gdx.audio.newSound(Gdx.files.internal("1UP.mp3"));
        bombSound       = Gdx.audio.newSound(Gdx.files.internal("error.mp3"));
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.play();
        backgroundMusic.setVolume(0.3f);

        viewport    = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        spriteBatch = new SpriteBatch();

        bucketSprite = new Sprite(bucketTexture);
        bucketSprite.setSize(BUCKET_SIZE, BUCKET_SIZE);
        bucketSprite.setPosition(WORLD_WIDTH / 2f - BUCKET_SIZE / 2f, 0);

        touchpos  = new Vector2();
        droplets  = new Array<>();
        dropTimer = 0f;
        bigDropTimer = 0f;
        bombTimer = 0f;

        // Fuente FreeType
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("PixelOperator8-Bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 16;
        font = generator.generateFont(parameter);
        generator.dispose();
    }

    @Override
    public void render(float delta) {
        if (!juegoTerminado) {
            input();
            logic(delta);
        }
        draw();
    }

    public void input() {
        float speed = 300f; // ajustado al mundo 800x500
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            bucketSprite.translateX(speed * delta);
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            bucketSprite.translateX(-speed * delta);

        if (Gdx.input.isTouched()) {
            touchpos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchpos);
            bucketSprite.setCenterX(touchpos.x);
        }
    }

    public void logic(float delta) {
        float dropSpeed = 250f;

        // Actualizar temporizador
        tiempoRestante -= delta;
        if (tiempoRestante <= 1) {
            tiempoRestante = 0;
            juegoTerminado = true;
            //backgroundMusic.stop();
            return;
        }

        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, WORLD_WIDTH - BUCKET_SIZE));

        dropTimer += delta;
        if (dropTimer >= DROP_INTERVAL) {
            dropTimer = 0f;
            createDroplet();
        }

        bigDropTimer += delta;
        if (bigDropTimer >= BIG_DROP_INTERVAL) {
            bigDropTimer = 0.45f;
            createBigDroplet();
        }

        bombTimer += delta;
        if (bombTimer >= BOMB_INTERVAL) {
            bombTimer = 0.45f;
            createBomb();
        }

        for (Sprite drop : droplets) {
            drop.translateY(-dropSpeed * delta);
        }

        // Hitbox reducida del cubo
        Rectangle bucketHitbox = new Rectangle(
            bucketSprite.getX() + 8f,
            bucketSprite.getY() + 8f,
            bucketSprite.getWidth() - 16f,
            bucketSprite.getHeight() * 0.6f
        );

        Array<Sprite> toRemove = new Array<>();
        for (Sprite drop : droplets) {
            if (drop.getBoundingRectangle().overlaps(bucketHitbox)) {

                if (drop.getWidth() == BIG_DROP_SIZE) {
                    puntaje += 3;
                    bigDropSound.play();
                }
                if (drop.getWidth() == BOMB_SIZE) {
                    bombSound.play();
                    if (puntaje > 0)
                        puntaje -= 3;
                    if (puntaje < 0)
                        puntaje = 0;
                }
                if (drop.getWidth() == DROP_SIZE) {
                    puntaje++;
                    dropSound.play();
                }
                toRemove.add(drop);
            } else if (drop.getY() + drop.getHeight() < 0) {
                toRemove.add(drop);
            }
        }
        droplets.removeAll(toRemove, true);
    }

    public void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        spriteBatch.draw(backgroundTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        if (!juegoTerminado) {
            for (Sprite drop : droplets) {
                drop.draw(spriteBatch);
            }
            bucketSprite.draw(spriteBatch);

            // Puntaje - esquina superior izquierda
            font.getData().setScale(2f);
            font.setColor(Color.WHITE);
            font.draw(spriteBatch, "Puntos: " + puntaje, 10, 490);

            // Temporizador - esquina superior derecha
            font.setColor(tiempoRestante < 5 ? Color.RED : Color.WHITE);
            font.draw(spriteBatch, "Tiempo: " + (int) tiempoRestante, 500, 490);

        } else {
            // Pantalla de fin de juego
            font.getData().setScale(3f);
            font.setColor(Color.YELLOW);
            font.draw(spriteBatch, "FIN DEL JUEGO", 120, 320);

            font.getData().setScale(2f);
            font.draw(spriteBatch, "Puntuacion final: " + puntaje, 120, 250);
            font.setColor(Color.WHITE);
            font.getData().setScale(1.5f);
            font.draw(spriteBatch, "Pulsa ENTER para volver al menu", 75, 190);

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                backgroundMusic.stop();
                game.setScreen(new MenuScreen(game));
            }
        }

        spriteBatch.end();
    }

    public void createDroplet() {
        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(DROP_SIZE, DROP_SIZE);
        dropSprite.setX(MathUtils.random(0, WORLD_WIDTH - DROP_SIZE));
        dropSprite.setY(WORLD_HEIGHT);
        droplets.add(dropSprite);
    }
    public void createBigDroplet() {
        Sprite dropBigSprite = new Sprite(dropBigTexture);
        dropBigSprite.setSize(BIG_DROP_SIZE, BIG_DROP_SIZE);
        dropBigSprite.setX(MathUtils.random(0, WORLD_WIDTH - BIG_DROP_SIZE));
        dropBigSprite.setY(WORLD_HEIGHT);
        droplets.add(dropBigSprite);
    }

    public void createBomb() {
        Sprite bombSprite = new Sprite(bombTexture);
        bombSprite.setSize(BOMB_SIZE, BOMB_SIZE);
        bombSprite.setX(MathUtils.random(0, WORLD_WIDTH - BOMB_SIZE));
        bombSprite.setY(WORLD_HEIGHT);
        droplets.add(bombSprite);
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        if (viewport != null) viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        dropTexture.dispose();
        dropBigTexture.dispose();
        bombTexture.dispose();
        bucketTexture.dispose();
        dropSound.dispose();
        bigDropSound.dispose();
        bombSound.dispose();
        backgroundMusic.dispose();
        spriteBatch.dispose();
        font.dispose();
    }
}
