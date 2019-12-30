package ch.epfl.gameboj;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import java.util.Objects;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
/**
 * Classe chargée de représenter un banc de registres 8 bits
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public final class RegisterFile<E extends Register> {
    private final byte[] registerArray;

    /**
     * Construit un banc de registre 8 bits
     * La taille du banc est égale à la taille du tableau donnée
     * @param allRegs: tableau dont on extrait la taille
     * @throws NullPointerException si allRegs est nul
     */
    public RegisterFile(E[] allRegs) {
        Objects.requireNonNull(allRegs);
        this.registerArray = new byte[allRegs.length];
    }

    /**
     * Retourne la valeur 8 bits contenus dans le registre donné, non signé
     * @param reg: registre donné
     * @return valeur 8 bits non signé cotenu dans le registre
     * @throws NullPointerException si reg est nul
     */
    public int get(E reg) {
        Objects.requireNonNull(reg);
        return Byte.toUnsignedInt(registerArray[reg.index()]);
    }
    
    /**
     * Modifie le contenue du registre donné pour qu'il soit égal à la valeur 8 bits donné
     * @param reg: registre donné
     * @param newValue: valeur à mettre dans le registre donné
     * @throws IllegalArgumentException si newValue n'est pas une valeur 8bit
     * @throws NullPointerException si reg est nul
     */
    public void set(E reg, int newValue) {
        checkBits8(newValue);
        Objects.requireNonNull(reg);
        registerArray[reg.index()] = (byte)newValue;
    }

    /**
     * Retourne vrai ssi le bit du registre donné vaut 1
     * @param reg: registre donné
     * @param b: index
     * @throws NullPointerException si reg est nul
     * @return: vrai ssi le bit vaut 1
     */
    public boolean testBit(E reg, Bit b) {
        Objects.requireNonNull(reg);
        return Bits.test(registerArray[reg.index()], b);
    }

    /**
     * Modifie la valeur stockée dans le registre donnée pour que le bit donné ait la nouvelle valeur donnée
     * @param reg: registre donné
     * @param bit: bit donné
     * @param newValue: valeur donnée (1 == true, 0 == false)
     * @throws NullPointerException si reg est nul
     */
    public void setBit(E reg, Bit bit, boolean newValue) {
        Objects.requireNonNull(reg);
        set(reg, Bits.set(get(reg), bit.index(), newValue));
    }	 
}


