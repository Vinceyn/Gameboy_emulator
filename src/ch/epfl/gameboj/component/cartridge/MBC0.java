package ch.epfl.gameboj.component.cartridge;
import static ch.epfl.gameboj.Preconditions.checkArgument;
import static ch.epfl.gameboj.Preconditions.checkBits16;

import java.util.Objects;

import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;


/**
 * Classe chargée de représenter un contrôleur de banque mémoire de type 0, c-à-d doté uniquement d'une mémoire morte de 32 768 octets
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public final class MBC0 implements Component {
    private Rom rom;
    private final static int CARTRIDGE_ROM_SIZE = 0x8000;

    /**
     * Construit un contrôleur de type 0 pour la mémoire donnée.
     * @param rom : la memoire donnée
     * @throws NullPointerException si la mémoire est nulle
     * @throws IllegalArgumentException si elle ne contient pas exactement 32 768 octets
     */
    public MBC0(Rom rom) {
        Objects.requireNonNull(rom);
        checkArgument(rom.size() == CARTRIDGE_ROM_SIZE);
        this.rom = rom;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        checkBits16(address);
        if((0 <= address) && (address < CARTRIDGE_ROM_SIZE)) {
            return rom.read(address);
        }
        else {
            return NO_DATA;
        }
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     * Ne verifie pas l'addresse ni la data (16bits et 8bits respectivement) car la méthode ne fait rien
     */
    @Override
    public void write(int address, int data) {
        return;
    }
}


