package ch.epfl.gameboj.component.memory;

import static ch.epfl.gameboj.Preconditions.checkArgument;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import java.util.Objects;

/**
 * Classe représentant une mémoire vive
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public class Ram {

    private final byte[] ramData;

    /**
     * Construit une nouvelle mémoire vive de taille donnée en octet
     * @throws IllegalArgumentException si la taille est strictement négatif
     * @param size: taille de la RAM
     */
    public Ram(int size){
        checkArgument(size >= 0); 
        ramData = new byte[size];
    }

    /**
     * Retourne la taille, en octet, de la mémoire
     * @return la taille, en octet, de la mémoire
     */
    public int size() {
        return ramData.length;
    }

    /**
     * Retourne l'octet se trouvant à l'index donné
     * @param index: index auquel on cherche l'octet à lire
     * @throws IndexOutOfBoundsException si l'index est invalide
     * @return l'octet se trouvant à l'index donné
     */
    public int read(int index) {
        if((index < 0) || (index > ramData.length)){
            throw new IndexOutOfBoundsException();
        }
        return Byte.toUnsignedInt(ramData[index]);
    }

    /**
     * Modifie le contenu de la mémoire à l'index donné pour qu'il soit égal à la valeur donnée
     * @param index: index auquel on va écrire l'octet
     * @param value: valeur qu'on souhaite mettre à l'index
     * @throws IndexOutOfBoundsException si l'index est invalide 
     * @throws IllegalArgumentException si la valeur n'est pas une valeur 8 bits
     */
    public void write(int index, int value) {
        Objects.checkIndex(index, ramData.length);
        checkBits8(value);
        ramData[index] = (byte) value;
    }

}
