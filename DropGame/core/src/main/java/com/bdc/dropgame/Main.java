package com.bdc.dropgame;

import com.badlogic.gdx.Game;

// Autor: Carlos Serradilla
public class Main extends Game {

    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }
}
