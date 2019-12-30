package ch.epfl.gameboj.bits;

/**
 * Interface ayant pour but d'être implémentée par les type énumérés représentant un ensemble de bits.
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public interface Bit {
    /**
     * Automatiquement fourni par le type énuméré, étant donné que tous ces types fournissent une telle méthode
     * @return: automatiquement fourni
     */
    public int ordinal();

    /**
     * Retourne la même valeur que la méthode ordinal mais dont le nom est plus parlant
     * @return: même valeur que la méthode ordinal
     */
    public default int index() {
        return ordinal();
    }

    /**
     * Retourne le masque correspondant au bit
     * @return le masque correspondant au bit
     */
    public default int mask() {
        return Bits.mask(index());
    }

}
