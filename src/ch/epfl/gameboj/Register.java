package ch.epfl.gameboj;

/**
 * Interface ayant pour but d'être implémentée par les types énumérés
 * représentant les registres d'un même banc.
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public interface Register {

    /**
     * Automatiquement fourni par le type énuméré, étant donné que tous ces types fournissent une telle méthode
     * @return ordinal() : automatiquement fourni
     */
    public int ordinal();

    /**
     * Retourne la même valeur que la méthode ordinal mais dont le nom est plus parlant
     * @return ordinal () : même valeur que la méthode ordinal
     */
    public default int index() {
        return ordinal();
    }
}

