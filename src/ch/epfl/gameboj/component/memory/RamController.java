package ch.epfl.gameboj.component.memory;

import static ch.epfl.gameboj.Preconditions.checkArgument;
import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import java.util.Objects;

import ch.epfl.gameboj.component.Component;

/**
 * Représent un composant contrôlant l'accès à une mémoire vive
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public final class RamController implements Component{

    private final Ram ramControlled;
    private final int startAddress;
    private final int endAddress;

    /**
     * Construit un contrôleur pour la mémoire vive donnée, accessible entre 
     * l'adresse startAddress (inclue) et endAddress (exclue)
     * @param ram: mémoire vive qui va être controlée
     * @param startAddress: adresse de départ de la RAM (inclue)
     * @param endAddress: adresse de fin de la RAM (exclue)
     * @throws NullPointerException si la mémoire donnée est nulle 
     * @throws IllegalArgumentException si l'une des deux adresses n'est pas une valeur 16 bits
     *         ou si l'intervalle qu'elles décrivent a une taille négative ou supérieure à celle de la mémoire
     */
    public RamController(Ram ram, int startAddress, int endAddress) {
        checkBits16(startAddress);
        checkBits16(endAddress);
        Objects.requireNonNull(ram);
        checkArgument((startAddress <= endAddress) && (endAddress - startAddress <= ram.size()));

        ramControlled = ram;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
    }

    /**
     * Construit un contrôleur de la mémoir vive donnée, accessible entre
     * l'adresse startAddress (inclue) et startAddress + taille de la ram (exclue)
     * @param ram: mémoire vive qui va être controlée
     * @param startAddress: adresse de départ de la RAM (inclue)
     * @throws NullPointerException si la mémoire donnée est nulle 
     * @throws IllegalArgumentException si l'une des deux adresses n'est pas une valeur 16 bits
     *         ou si l'intervalle qu'elles décrivent a une taille négative ou supérieure à celle de la mémoire
     */
    public RamController(Ram ram, int startAddress) {
        this(ram, startAddress, startAddress + ram.size());
    }


    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override 
    public int read(int address) {
        
        checkBits16(address);
        if ((startAddress <= address ) && (address < endAddress)) {
            return ramControlled.read(address - startAddress);
        }
        else {
            return NO_DATA;
        }
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        checkBits16(address);
        checkBits8(data);
        if((address >= startAddress ) && (address < endAddress)) {
            ramControlled.write(address - startAddress, data);
        }
    }
}
