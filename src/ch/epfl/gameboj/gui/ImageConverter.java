package ch.epfl.gameboj.gui;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * Classe représentant convertisseur d'image Game Boy en image JavaFX
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public final class ImageConverter {

    private static final int LCD_WIDTH = 160;
    private static final int LCD_HEIGHT = 144;
    private static final int ALPHA = 0xFF << 24;
    private static final int[] COLOR_MAP = new int[] {
            0xFF_FF_FF, 0xD3_D3_D3, 0xA9_A9_A9, 0x00_00_00
    };

    /**
     * Convertis l'image Gameboy en image JavaFX
     * @param image : du type LcdImage à convertir
     * @return une image de type javafx.scene.image.Image.
     */
    public static javafx.scene.image.Image convert(LcdImage image) {
        WritableImage wrIm = new WritableImage(LCD_WIDTH, LCD_HEIGHT);
        PixelWriter pw = wrIm.getPixelWriter();

        for(int y = 0; y < LCD_HEIGHT; ++y) 
            for(int x = 0; x < LCD_WIDTH; ++x) 
                pw.setArgb(x, y, ALPHA | COLOR_MAP[image.get(x,y)]);
        return wrIm;
    }
    
}


