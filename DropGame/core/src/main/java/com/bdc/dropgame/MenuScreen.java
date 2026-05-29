package com.bdc.dropgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class MenuScreen implements Screen {

    Main game;
    Texture backgroundTexture;
    FitViewport viewport;
    SpriteBatch spriteBatch;
    ShapeRenderer shapeRenderer;
    BitmapFont font;
    Music menuMusic;

    Rectangle botonJugar = new Rectangle(300, 200, 200, 60);

    boolean puedeTocar = false;
    float timerTocar   = 0f;

    public MenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        backgroundTexture = new Texture("backgroundMenu.png");
        viewport      = new FitViewport(800, 500);
        spriteBatch   = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("PixelOperator8-Bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 16;
        font = generator.generateFont(parameter);
        generator.dispose();

        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("musicMenu.mp3"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(0.3f);
        menuMusic.play();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();

        // Dibujar botón
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(botonJugar.x, botonJugar.y, botonJugar.width, botonJugar.height);
        shapeRenderer.end();

        // Dibujar fondo y textos
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        spriteBatch.draw(backgroundTexture, 0, 0, 800, 500);

        font.setColor(Color.YELLOW);
        font.getData().setScale(3f);
        font.draw(spriteBatch, "DROP GAME", 200, 400);

        font.setColor(Color.WHITE);
        font.getData().setScale(1f);
        font.draw(spriteBatch, "Créditos musicales:", 10, 75);
        font.draw(spriteBatch, "Menú: Frog summer - Leon Chang", 10, 50);
        font.draw(spriteBatch, "Juego principal: Anyone in 2025? - Sharou", 10, 25);

        font.getData().setScale(2.4f);
        font.draw(spriteBatch, "JUGAR", 300, 240);

        spriteBatch.end();

        // Esperar 0.1s antes de aceptar toques
        if (!puedeTocar) {
            timerTocar += delta;
            if (timerTocar > 0.1f) puedeTocar = true;
            return;
        }

        // Detectar clic sobre el botón
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.getCamera().unproject(touchPos, viewport.getScreenX(), viewport.getScreenY(),
                viewport.getScreenWidth(), viewport.getScreenHeight());

            if (botonJugar.contains(touchPos.x, touchPos.y)) {
                menuMusic.stop();
                game.setScreen(new GameScreen(game));
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        spriteBatch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        if (menuMusic != null) {
            menuMusic.stop();
            menuMusic.dispose();
            menuMusic = null;
        }
    }
}
