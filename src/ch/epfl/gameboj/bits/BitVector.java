package ch.epfl.gameboj.bits;
import static ch.epfl.gameboj.Preconditions.checkArgument;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import java.util.Arrays;
import java.util.Objects;

/**
 * Classe chargée de représenter le processeur de la gameboy
 * @author Niels Escarfail (282347)
 * @author Vincent Yuan (287639)
 */
public final class BitVector {
    
    private static final int FULL_OF_1 = -1; //-1 integer's representation is 32's 1
    
    private final int[] bitVector;
    
    /**
     * Enumération représentant les différents types d'extraction
     */
    private enum ExtractType {
        ZERO_EXTENDED, WRAPPED
    }

    
    /**
     * Construit un vecteur de bits de la taille donnée, dont tous les bits ont la valeur donnée
     * @param size : la taille du vecteur (un multiple de 32 strictement positif)
     * @param initialValue : booleen donnant la valeur de tous les bits
     * @throws IllegalArgumentException, si la taille n'est pas un multiple de 32 strictement positif
     */
    public BitVector(int size, boolean initialValue) {
        checkArgument((size % Integer.SIZE == 0) && (size > 0));
        bitVector = new int[size / Integer.SIZE];
        Arrays.fill(bitVector, initialValue ? FULL_OF_1 : 0);
    }

    /**
     * Construit un vecteur de bits de la taille donnée, dont tous les bits sont a 0
     * @param size : la taille du vecteur (un multiple de 32 strictement positif)
     */
    public BitVector(int size) {
        this(size, false);
    }

    /**
     * Stocke (sans le copier) le tableau donné en argument à un attribut de l'instance
     * @param stockArray : tableau de int contenant les elements du vecteur
     */
    private BitVector(int[] stockArray) {
        bitVector = stockArray;
    }


    /**
     * Donne la taille du vecteur, en bits
     * @return la taille du vecteur
     */
    public int size() {
        return bitVector.length * Integer.SIZE;
    }


    /**
     * Détermine si le bit d'index donné est vrai ou faux 
     * @param index : l'index donné
     * @return la valeur du bit d'index donné
     * @throws IllegalArgumentException si l'index ne désigne aucun bit du vecteur
     */
    public boolean testBit(int index){
        Objects.checkIndex(index, size());
        return Bits.test(bitVector[index / Integer.SIZE], Math.floorMod(index, Integer.SIZE));
    }


    /**
     * Calcule le complément du vecteur de bits 
     * @return un nouveau vecteur de bits, le complément du vecteur sur lequel on applique la méthode
     */
    public BitVector not() {
        int[] newBitVector = new int[bitVector.length];
        for (int i = 0; i < bitVector.length; ++i) {
            newBitVector[i] = ~bitVector[i];
        }
        return new BitVector(newBitVector);
    }



    /**
     * Calcule la disjonction (or) bit à bit avec un autre vecteur de même taille
     * @param scdBitVector : le vecteur avec lequel on compare le vecteur de l'instance
     * @return un nouveau vecteur de bits, la disjonction bit à bit avec le vecteur passé en argument
     * @throws IllegalArgumentException si les vecteurs n'ont pas la même taille
     */
    public BitVector or(BitVector scdBitVector) {
        checkArgument(bitVector.length == scdBitVector.bitVector.length);
        int[] newBitVector = new int[bitVector.length];
        for (int i = 0; i < bitVector.length; ++i) {
            newBitVector[i] = bitVector[i] | scdBitVector.bitVector[i];
        }
        return new BitVector(newBitVector);
    }


    /**
     * Calcule la conjonction (and) bit à bit avec un autre vecteur de même taille
     * @param scdBitVector : le vecteur avec lequel on compare le vecteur de l'instance
     * @return un nouveau vecteur de bits, la conjonction bit à bit avec le vecteur passé en argument
     * @throws IllegalArgumentException si les vecteurs n'ont pas la même taille
     */
    public BitVector and(BitVector scdBitVector) {
        checkArgument(bitVector.length == scdBitVector.bitVector.length);
        int[] newBitVector = new int[bitVector.length];
        for (int i = 0; i < bitVector.length; ++i) {
            newBitVector[i] = bitVector[i] & scdBitVector.bitVector[i];
        }
        return new BitVector(newBitVector);
    }


    /**
     * Extrait un vecteur de taille donnée de l'extension par 0 du vecteur de l'instance
     * @param index : l'index d'où commence l'extraction
     * @param size : la taille du vecteur à extraire
     * @return un nouveau vecteur de bits, l'extraction de l'extension par 0 du vecteur
     */
    public BitVector extractZeroExtended(int index, int size) {
        return extract(index, size, ExtractType.ZERO_EXTENDED);
    }


    /**
     * Extrait un vecteur de taille donnée de l'extension par enroulement du vecteur de l'instance
     * @param index : l'index d'où commence l'extraction
     * @param size : la taille du vecteur à extraire
     * @return un nouveau vecteur de bits, l'extraction de l'extension par enroulement du vecteur
     */
    public BitVector extractWrapped(int index, int size) {
        return extract(index, size, ExtractType.WRAPPED);
    }


    /**
     * Décale le vecteur d'une distance quelconque
     * @param distance : une distance positive représente un décalage à gauche, une distance négative un décalage à droite
     * @return un nouveau vecteur de bits, donné par le vecteur de l'instance après décallage
     */
    public BitVector shift(int distance) {
        return extractZeroExtended(-distance, this.size());
    }


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof BitVector && Arrays.equals(bitVector, ((BitVector)o).bitVector);
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(bitVector);
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < size(); ++i) { 
            b.append(testBit(i) ? 1 : 0);
        }
        return b.reverse().toString();
    }

    /**
     * Effectue la méthode extract selon le type d'extraction demandé
     * @param index : l'index d'où commence l'extraction
     * @param size : la taille du vecteur à extraire
     * @param type : le type de l'extraction (extension par 0 ou par enroulement)
     * @return un nouveau vecteur de bits, l'extraction de l'extension du vecteur
     * @throws IllegalArgumentException si la taille est inférieure à 0 ou 
     */
    private BitVector extract(int index, int size, ExtractType type) {
        checkArgument(size > 0 && size%Integer.SIZE == 0);

        int[] bitGroupsFromExtract = new int[Math.floorDiv(size, Integer.SIZE)];

        int bits = Math.floorDiv(index, Integer.SIZE); 

        if(Math.floorMod(index, Integer.SIZE) == 0) {
            for (int i = 0; i < bitGroupsFromExtract.length; ++i) {
                bitGroupsFromExtract[i] = getElementFromInfExtension(i + bits, type);
            }
        }
        else {
            for (int i = 0; i < bitGroupsFromExtract.length; ++i) {
                bitGroupsFromExtract[i] = getElementFromInfExtension(i + bits + 1, type) << (Integer.SIZE - index);
                bitGroupsFromExtract[i] += getElementFromInfExtension(i + bits, type) >>> index;
            }
        }
        return new BitVector(bitGroupsFromExtract);
    }


    /**
     * Méthode utilitaire pour extraire une valeur (int) de l'extension infinie du vecteur
     * @param index : l'index d'où commence l'extraction
     * @param type : le type de l'extraction (extension par 0 ou par enroulement)
     * @return la valeur 32 bits extraite, à reconstituer afin d'obtenir l'extraction
     */
    private int getElementFromInfExtension(int index, ExtractType type) {
        if (type.equals(ExtractType.ZERO_EXTENDED)) {
            return (0 <= index && index < bitVector.length) ? bitVector[index] : 0;

        } else {
            return bitVector[Math.floorMod(index, bitVector.length)];
        }
    }


    /**
     * Classe imbriquée représentant un bâtisseur de vecteur de bits.
     * Permet la construction d'une vecteur de bits de manière incrémentale, octet par octet.
     */



    public static final class Builder {

        private int[] vector;
        private static final int FIRST_4_BYTES = 4;


        /**
         * Construit un batisseur de vecteur de bits de la taille donnée, dont tous les bits sont a 0
         * @param size : la taille du vecteur de bits à construire (un multiple de 32 strictement positif)
         * @throws IllegalArgumentException, si la taille n'est pas un multiple de 32 strictement positif
         */
        public Builder(int size) {
            checkArgument(size > 0 && size % Integer.SIZE == 0);
            vector = new int[size / Integer.SIZE];
            Arrays.fill(vector, 0);
        }

        /**
         * Défini la valeur d'un octet désigné par son index
         * @param index : l'index de l'octet
         * @param byteToAdd : la valeur que l'on assigne à l'octet désigné
         * @return le Builder dont la valeur de l'octet désigné a été modifié
         * @throws IndexOutOfBoundsException si l'index ne désigne pas un octet du Builder
         * @throws IllegalArgumentException si la valeur à assigner n'est pas 8 bits
         * @throws IllegalStateException si la méthode setByte est appelée ultérieurement au batissage du vecteur
         */
        public BitVector.Builder setByte(int index, int byteToAdd) {
            checkState();
            Objects.checkIndex(index, vector.length*Integer.BYTES);
            checkBits8(byteToAdd);

            int shift = (index % Integer.SIZE) * Byte.SIZE;
            int subIndex = (index < FIRST_4_BYTES) ? 0 : Math.floorDiv(index, Integer.BYTES);

            vector[subIndex] += (byteToAdd << shift);

            return this;
        }


        /**
         * Construit le vecteur de bits à partir du byteVector de l'instance
         * @return le BitVector crée à l'aide du Builder
         * @throws IllegalStateException si la méthode build est appelée ultérieurement au batissage du vecteur
         */
        public BitVector build() {  
            checkState();
            BitVector newVect = new BitVector(vector);
            vector = null;
            return newVect;
        }

        /**
         * Verifie que l'on agit pas sur le vecteur ultérieurement au batissage du vecteur
         * @throws IllegalStateException si l'on agit pas sur le vecteur ultérieurement au batissage du vecteur
         */
        private void checkState() {
            if (vector == null)
                throw new IllegalStateException();
        }
    }
}