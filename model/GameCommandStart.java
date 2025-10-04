package model;

import controller.KeyHandler;

public class GameCommandStart implements Command {


    @Override
    public void execute() {
        System.out.println("Jogo Iniciado");
        KeyHandler.gamestart = true;
    }
}
