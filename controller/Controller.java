package controller;

import java.util.ArrayList;

import model.Command;

public class Controller {
    ArrayList<Command> slots = new ArrayList<Command>();

    public void setSlot(Command command)
    {
        slots.add(command);
    }

    public void pressbuton(int index)
    {
        if(slots.isEmpty())
        {
            System.out.println("As vagas estão vazias");
        }
        else
        {
            Command command = slots.get(index);
            if(command!=null)
            {
                command.execute();

            }
            else
            {
                System.out.println("Nenhum comando atribuído a este botão");
            }
        }
    }
}
