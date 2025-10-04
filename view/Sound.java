package view;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class Sound {

    Clip musicClip;

    URL[] url = new URL[10];

    public Sound(){
        url[0]= getClass().getResource("tile.wav");
        url[1]= getClass().getResource("delete line.wav");
        url[2]= getClass().getResource("gameover.wav");
        url[3]= getClass().getResource("rotation.wav");
        url[4]= getClass().getResource("touch floor.wav");
    }

    public void play(int i , boolean music){
        try{
            // Evita criar outra trilha se já está tocando música de fundo
            if(music && musicClip != null && musicClip.isRunning()){
                return; // já tocando, não duplica
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url[i]);
            Clip clip = AudioSystem.getClip();

            if(music){
                // Se havia um clip antigo parado, liberar
                if(musicClip != null && musicClip.isOpen() && !musicClip.isRunning()){
                    musicClip.close();
                }
                musicClip = clip;
            }
            clip.open(ais);
            clip.addLineListener(event -> {
                if(event.getType() == LineEvent.Type.STOP){
                    clip.close();
                }
            });
            ais.close();
            clip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            throw new RuntimeException(ex);
        }

    }
    public void loop(){
        if(musicClip != null && musicClip.isOpen()){
            // Só coloca em loop se não estiver já rodando continuamente
            if(!musicClip.isRunning()){
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                // Garantir modo loop mesmo se já rodando
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }
    }
    public void stop(){
        if(musicClip!=null){
            if(musicClip.isRunning()){
                musicClip.stop();
            }
            if(musicClip.isOpen()){
                musicClip.close();
            }
            musicClip = null;
        }

    }

    // Método auxiliar para verificar se já há música tocando
    public boolean isMusicPlaying(){
        return musicClip != null && musicClip.isRunning();
    }
}
