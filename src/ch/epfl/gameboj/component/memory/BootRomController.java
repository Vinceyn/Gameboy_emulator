package ch.epfl.gameboj.component.memory;
import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;

/**
 * Classe chargée de représenter le contrôleur de la mémoire morte de démarrage
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public final class BootRomController implements Component {
    private Rom bootRom;
    private boolean isUsed;
    private Cartridge cartridge;

    /**
     * Construit un contrôleur de mémoire de démarrage auquel la cartouche donnée est attachée.
     * @param cartridge : la cartouche donnée
     * @throws NullPointerException si cette cartouche est nulle
     */
    public BootRomController(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        this.cartridge = cartridge;
        isUsed = true;
        bootRom = new Rom(BootRom.DATA);
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        checkBits16(address);
        if (readInterceptsAddress(address)) {
            return bootRom.read(address - AddressMap.BOOT_ROM_START);
        }
        else {
            return cartridge.read(address);
        }
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        checkBits16(address); 
        checkBits8(data);
        if (writeDisablesBootRom(address)) {
            isUsed = false;
        }
        else {
            cartridge.write(address, data);
        }
    }

    /**
     * @param address : l'addresse donnée dans read
     * @return si l'addresse doit être interceptée par le BootRomController pour être lue.
     */
    private boolean readInterceptsAddress(int address) {
        return AddressMap.BOOT_ROM_START <= address && address < AddressMap.BOOT_ROM_END && isUsed;
    }




    /**
     * Dit si l'addresse donnée indique de désactiver la mémoire de démarrage
     * @param address : une addresse tirée de write
     * @return si l'addresse donnée indique de désactiver la mémoire de démarrage
     */
    private boolean writeDisablesBootRom(int address) {
        return address == AddressMap.REG_BOOT_ROM_DISABLE && isUsed;
    }
}

