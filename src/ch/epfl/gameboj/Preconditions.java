package ch.epfl.gameboj;

/**
 * Permet de s'assurer que les méthodes du programmes vont être appelées avec des arguments valides
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public interface Preconditions {

    public final static int MAX8BITS = 0xFF;
    public final static int MAX16BITS = 0xFFFF;

    /**
     * S'assure de la validité d'un argument 
     * @param b: la condition qu'on veut tester
     * @throws IllegalArgumentException si la condition n'est pas vérifiée
     */
    public static void checkArgument(boolean b) {
        if (!b) {
            throw new IllegalArgumentException();
        }
    }


    /**
     * S'assure que l'argument passé peut être représenté en 8 bits
     * @param v: argument à tester
     * @throws IllegalArgumentException si l'argument ne peut pas être représenté en 8 bits
     * @return la valeur si elle peut être représenté en 8 bits
     */
    public static int checkBits8(int v) {
        checkArgument((0 <= v) && (v <= MAX8BITS));
        return v;
    }


    /**
     * S'assure que l'argument passé peut être représenté en 16 bits
     * @param v: argument à tester
     * @throws IllegalArgumentException si l'argument ne peut pas être représenté 16 bits
     * @return la valeur si elle peut être représenté en 16 bits
     */
    public static int checkBits16(int v) {
        checkArgument((0 <= v) && (v <= MAX16BITS));
        return v;
    }

}
