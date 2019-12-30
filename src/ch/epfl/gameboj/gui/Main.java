package ch.epfl.gameboj.gui;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Classe classe contenant le programme principal du simulateur
 * @author Niels Escarfail (282347)
 * @author Vincent Yuan (287639)
 */
public final class Main extends Application {

    private static final int SCALED_WIDTH = 320;
    private static final int SCALED_HEIGHT = 288;

    /* Map les touches du clavier à celui de la Gameboy*/
    private final Map<KeyCode, Joypad.Key> KEYS = new HashMap<KeyCode, Joypad.Key>() {{
        put(KeyCode.RIGHT, Joypad.Key.RIGHT);
        put(KeyCode.LEFT, Joypad.Key.LEFT);
        put(KeyCode.UP, Joypad.Key.UP);
        put(KeyCode.DOWN, Joypad.Key.DOWN);
        put(KeyCode.A, Joypad.Key.A);
        put(KeyCode.B, Joypad.Key.B);
        put(KeyCode.S, Joypad.Key.START);
        put(KeyCode.SPACE, Joypad.Key.SELECT);
    }};

    /**
     * Appelle la methode launch de Application, en lui passant les arguments recus.
     * @param args : les arguments recus.
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    /* (non-Javadoc)
     * @see javafx.application.Application#start(javafx.stage.Stage)
     * Termine l'execution si le nom du fichier ROM n'a pas été passé au programme.
     * Cree la gameboy dont la cartouche est obtenue par le fichier ROM
     * Cree l'interface graphique et l'affiche à l'ecran
     * Simule la gameboy
     */
    @Override 
    public void start(Stage stage) throws IOException, InterruptedException {
        if (getParameters().getRaw().size() != 1)
            System.exit(1);

        File romFile = new File(getParameters().getRaw().get(0));             
        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));

        ImageView imageView = new ImageView();
        imageView.setImage(getImage(gb));
        imageView.setSmooth(true);

        BorderPane root = new BorderPane(imageView);
        Scene scene = new Scene(root);

        imageView.setFitWidth(SCALED_WIDTH);
        imageView.setFitHeight(SCALED_HEIGHT);
        
        stage.setScene(scene);      
        stage.setTitle("gameboj");
        stage.show();
        stage.requestFocus();
        
        Joypad joypad = gb.joypad();
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            Joypad.Key inputKey = KEYS.get(key.getCode());
            if (inputKey != null) {
                joypad.keyPressed(inputKey);
            }
        });
        
        scene.addEventHandler(KeyEvent.KEY_RELEASED, (key) -> {
            Joypad.Key inputKey = KEYS.get(key.getCode());
            if (inputKey != null) {
                joypad.keyReleased(inputKey);
            }
        });

        long start = System.nanoTime();
        new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                long cyclesElapsed = (long) ((currentNanoTime - start) * GameBoy.cyclesPerNanosec);
                gb.runUntil(cyclesElapsed);
                imageView.setImage(getImage(gb));
            }
        }.start();
    }

    /**
     * Donne l'image convertie apparaissant sur la gameboy à l'instant donné
     * @param gb : la gameboy
     * @return
     */
    private Image getImage(GameBoy gb) {
        return ImageConverter.convert(gb.lcdController().currentImage());
    }
}

