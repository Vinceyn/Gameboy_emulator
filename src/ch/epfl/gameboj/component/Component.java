package ch.epfl.gameboj.component;

import ch.epfl.gameboj.Bus;

/**
 * Interface représentant un composant du GameBoy connecté aux bus d'adresses et de données 
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail(282347)
 *
 */
public interface Component {
    
    
    public static final int NO_DATA = 0x100;

    /**
     * Lit l'octet stocké à l'adress, ou NO_DATA si le composant ne possède aucune valeur à cette adresse
     * @param address: adresse à laquelle on veut lire l'octet
     * @throws IndexOutOfBoundsException: si l'index est invalide
     * @return: l'octet stocké à l'adresse donnée par le composant, ou NO_DATA si le composant ne possède aucune valeur à cette adresse
     */
    public int read(int address);
    
    /**
     * Stocke la valeur donnée à l'adresse donnée dans le composant, ou ne fait rien si le composant ne permet pas de stocker 
     * de valeur à cette adresse
     * @param address: adresse à laquelle on va stocker la valeur donnée
     * @param data: valeur qu'on va stocker dans l'adresse
     * @throws IllegalArgumentException: si l'adresse n'est pas une valeur 16 bits ou si la donnée n'est pas une valeur 8 bits
     */
    public void write(int address, int data);
    
    /**
     * Attache le composant au bus donné
     * @param bus: bus auquel on va rattacher le composant
     */
    public default void attachTo(Bus bus) {
        bus.attach(this);
    }
    
    
}
