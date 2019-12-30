package ch.epfl.gameboj.component;
import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;

/**
 * Classe représentant le joypad de la gameboy
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public class Joypad implements Component {
    private static final int SAVE_4_MSB = 0xF0;
    private static final int TAKE_LINES = 0b00110000;
    private static final int NB_OF_LINES = 2;
    private static final int NB_OF_KEY_IN_A_LINE = 4;
    private static final int LENGTH_4 = 4;
    private static final int LENGTH_6 = 6;
    
    private final Cpu cpu;
    private int P1;
    private int[] lign;

    /*
     * Enumération représentant les bits du registre P1
     */
    private enum P1Bits implements Bit {
        COL0, COL1, COL2, COL3, LIGN0, LIGN1 
    }

    /*
     * Enumerations représentant les différentes touches du joypad
     */
    public enum Key implements Bit {
        RIGHT, LEFT, UP, DOWN, A, B, SELECT, START;

        /*
         * Méthode permettant d'obtenir la ligne de la touche
         */
        private int getLine() {
            return this.index() / NB_OF_KEY_IN_A_LINE;
        }

        /*
         * Méthode permettant d'obtenir la colonne de la touche
         */
        private int getColumn() {
            return this.index() % NB_OF_KEY_IN_A_LINE;
        }
    }

    /**
     * Constructeur du Joypad, qui lui associe le CPU et initialise le tableau représentant les lignes
     * @param cpu: cpu du joypad
     */
    public Joypad(Cpu cpu) {
        this.cpu = cpu;
        lign = new int[NB_OF_LINES];
    }

    
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        checkBits16(address);
        updateReg();
        return address == AddressMap.REG_P1 ? Bits.complement8(Bits.clip(LENGTH_6, P1)) : NO_DATA;
    }

    
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        checkBits16(address);
        checkBits8(data);
        if(address == AddressMap.REG_P1) {
            int previousP1Value = P1;
            int newData = Bits.clip(LENGTH_4, P1) | (Bits.complement8(data & TAKE_LINES)) ; 
            P1 = newData; 
            updateReg();
            test(previousP1Value);
        }
    }
    
    /**
     * Représente l'appui sur une touche du joypad
     * Modifie le bit représentant la colonne de la clé à la ligne de la clé, puis met à jour le registre P1
     * Puis test si une interruption doit être levée selon la valeur actuelle de P1 et la valeur précédente.
     * @param key: touche du joypad pressée
     */
    public void keyPressed(Key key) {
        int previousP1Value = P1;
        keyHelper(key, true);  
        updateReg();
        test(previousP1Value);
    }

    
    /**
     * Représente le relâchement d'une touche du joypad
     * Modifie le bit représentant la colonne de la clé à la ligne de la clé, puis met à jour le registre P1
     * @param key: touche de joypad relâché
     */
    public void keyReleased(Key key) {
        keyHelper(key, false);
        updateReg();
    }


    /*
     * Check si un bit de P1 est passée de 0 à 1 à l'aide de la valeur précédente de P1.
     * Si oui, lève l'interruption Joypad du processeur
     */
    private void test(int previousP1Value) {
        if ((~previousP1Value & P1)  != 0){
            cpu.requestInterrupt(Cpu.Interrupt.JOYPAD);
        }
    }

    /*
     * Selon l'état des bits LIGN0 et LIGN1, met à jour le registre P1 en faisant un "ou" logique avec les ligne
     */
    private void updateReg() {
        P1 = P1 & SAVE_4_MSB;
        
        if (testP1Bit(P1Bits.LIGN0))
            P1 = P1 | lign[0];
            
        
        if (testP1Bit(P1Bits.LIGN1))
            P1 = P1 | lign[1];
    }

    /*
     * Test un bit de P1 donné
     */
    private boolean testP1Bit(P1Bits b) {
        return Bits.test(P1, b);
    }

    /*
     * Change la valeur de la clé selon le boolean, en cherchant sa valeur et sa colonne à l'aide des méthodes propres à l'énumération key
     */
    private void keyHelper(Key key, boolean value) {
        lign[key.getLine()] = Bits.set(lign[key.getLine()], key.getColumn(), value);
    }
}


