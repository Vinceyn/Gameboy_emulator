package ch.epfl.gameboj.component.memory;

import java.util.Arrays;
import java.util.Objects;

/**
 * Classe représentant une mémoire morte 
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public final class Rom {

    private final byte[] romData;

    /**
     * Constructeur de la mémoire morte, dont le contenu et la taille sont ceux du tableau lancé en argument
     * @param data: tableau représentant une mémoire morte
     * @throws NullPointerException si le tableau passé en argument est null
     */
    public Rom(byte[] data){
        Objects.requireNonNull(data);
        romData = Arrays.copyOf(data, data.length);
    }

    /**
     * Retourne la taille, en octet de la mémoire morte
     * @return la taille en octet de la mémoire morte
     */
    public int size() {
        return romData.length;
    }

    /**
     * Lit la valeur à l'index donné
     * @param index: index auquel on cherche la valeur dans la ROM
     * @throws IndexOutBoundsException si l'index est invalide
     * @return l'octet se trouvant à l'index donné
     */
    public int read(int index) {
        if((0 > index) || (index > romData.length)){
            throw new IndexOutOfBoundsException();
        }
        return Byte.toUnsignedInt(romData[index]);
    }


}