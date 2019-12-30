package ch.epfl.gameboj.component.cpu;

import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import java.util.Objects;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * Classe chargée d'effectuer des opérations sur des valeurs 8 ou 16 bits et d'obtenir à la fois le résultat et la valeur des fanions
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public final class Alu {

    private Alu() {}

    private static final int BIT_9 = 8;
    private static final int BIT_8 = 7;
    private static final int BIT_7 = 6;
    private static final int BIT_5 = 4;
    private static final int BIT_1 = 0;
    private static final int LENGTH_8 = 8;
    private static final int LENGTH_9 = 9;
    private static final int LENGTH_16 = 16;
    private static final int LENGTH_4 = 4;
    private static final int MAX4BITS = 0xF;
    private static final int MAX8BITS = 0xFF;

    public static enum Flag implements Bit{
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, C, H, N, Z
    }


    public static enum RotDir{
        LEFT, RIGHT
    }

    /**
     * Retourne une valeur dont les bits correspondant aux différents fanions valent 1 ssi l'argument correspondant est vrai
     * @param z: fanion z
     * @param n: fanion n
     * @param h: fanion h
     * @param c: fanion c
     * @return: valeur dont les bits correspondant aux différents fanions valent 1 ssi l'argument correspondant est vrai
     */
    public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
        int mask = 0;
        if (z) mask += Flag.Z.mask();
        if (n) mask += Flag.N.mask();
        if (h) mask += Flag.H.mask();
        if (c) mask += Flag.C.mask();
        return mask;
    }



    /**
     * Retourne la valeur contenue dans le paquet valeur/fanion donnée 
     * @param valueFlags: paquet valeur/fanion donnée
     * @return valeur contenue dans valueFlags
     */
    public static int unpackValue(int valueFlags) {
        return valueFlags >>> LENGTH_8;
    }

    /**
     * Retourne les fanions contenus dans le paquet valeur/fanion donnée
     * @param valueFlags: paquet valeur/fanion donnée
     * @return: fanion contenue dans valueFlags
     */
    public static int unpackFlags(int valueFlags) {
        return Bits.clip(LENGTH_8, valueFlags);
    }


    /**
     * Fait la somme des deux valeurs 8 bits donnée et du bit de retenue initial c0
     * Fanions: Z0HC
     * @param l: valeur 8 bits
     * @param r: valeur 8 bits
     * @param c0: bit de retenue initiale
     * @throws: IllegalArgumentException si les valeurs ne peuvent pas être représentées en 8 bits
     * @return somme des deux valeurs donnée et du bit c0 + fanions
     */
    public static int add(int l, int r, boolean c0) {
        checkBits8(l);
        checkBits8(r);
         
        int carry = c0 ? 1 : 0;
        int value = Bits.clip(LENGTH_8, (l + r + carry));
        boolean z = (value == 0);
        boolean c = ((l + r + carry) > MAX8BITS);
        int hHelp = (Bits.clip(LENGTH_4, l) + Bits.clip(LENGTH_4, r));
        boolean h = ((hHelp + carry) > MAX4BITS);

        return packValueZNHC(value, z, false, h, c);
    }

    /**
     * Retourne la somme des deux valeurs 8 bits données, sans retenue initiale 
     * Fanion: Z0HC
     * @param l: valeur 8 bits
     * @param r: valeur 8 bits
     * @throws: IllegalArgumentException si les valeurs ne peuvent pas être représentées en 8 bits
     * @return somme des deux valeurs données + fanions
     */
    public static int add(int l, int r) {
        checkBits8(l);
        checkBits8(r);
        return add(l, r, false);
    }

    /**
     * Retourne la somme des deux valeurs 16 bits données
     * Fanions: 00HC, H et C correspondent à l'addition des 8 LSB
     * @param l: valeur 16 bits
     * @param r: valeur 16 bits
     * @throws: IllegalArgumentException si les valeurs ne peuvent pas être représentées en 16 bits
     * @return somme des deux valeurs 16 bits données + fanions
     */
    public static int add16L(int l, int r) {
        checkBits16(l);
        checkBits16(r);

        int value = Bits.clip(LENGTH_16, (l + r));
        boolean h = ((Bits.clip(LENGTH_4, l) + Bits.clip(LENGTH_4, r)) > MAX4BITS);
        boolean c = ((Bits.clip(LENGTH_8, r) + Bits.clip(LENGTH_8, l)) > MAX8BITS);

        return packValueZNHC(value, false, false, h, c);
    }

    /**
     * Retourne la somme des deux valeurs 16 bits données
     * Fanions: 00HC, H et C correspondent à l'addition des 8 MSB
     * @param l: valeur 16 bits
     * @param r: valeur 16 bits
     * @throws: IllegalArgumentException si les valeurs ne peuvent pas être représentées en 8 bits
     * @return somme des deux valeurs 16 bits données + fanions
     */
    public static int add16H(int l, int r) {
        checkBits16(l);
        checkBits16(r);

        int value = Bits.clip(LENGTH_16, (l + r));
        int carry = Bits.test(Bits.clip(LENGTH_8, l) + Bits.clip(LENGTH_8, r), BIT_9) ? 1 : 0; 
        boolean h = ((Bits.extract(l, LENGTH_8, LENGTH_4) + Bits.extract(r, LENGTH_8, LENGTH_4) + carry) > MAX4BITS);
        boolean c = ((Bits.extract(l, LENGTH_8, LENGTH_8) + Bits.extract(r, LENGTH_8, LENGTH_8) + carry) > MAX8BITS);

        return packValueZNHC(value, false, false, h, c);
    }

    /**
     * Retourne la différence des valeurs de 8 bits données et du bit d'emprunt b0
     * Fanions: Z1HC
     * @param l: valeur 8 bit 
     * @param r: valeur 8 bit à soustraire
     * @param b0: valeur de l'emprunt
     * @throws: IllegalArgumentException si les valeurs ne peuvent pas être représentées en 8 bits
     * @return différences des valeur 8 bits données + fanions
     */
    public static int sub(int l, int r, boolean b0) {
        checkBits8(l);
        checkBits8(r);        
        
        int value = b0 ? Bits.clip(LENGTH_8, (l-r-1)) : Bits.clip(LENGTH_8, (l-r));
        boolean z = (value == 0);
        int hHelp = (Bits.clip(LENGTH_4, l) - Bits.clip(LENGTH_4, r));
        boolean h = b0 ? ((hHelp - 1) < 0) : (hHelp < 0);
        boolean c = b0 ? (l < (r + 1)) : (l < r);

        return packValueZNHC(value, z, true, h, c);
    }

    /**
     * Retourne la différence des valeurs de 8 bits données, sans emprunt initial
     * Fanions: Z1HC
     * @param l: valeur 8 bit 
     * @param r: valeur 8 bit à soustraire
     * @throws: IllegalArgumentException si les valeurs ne peuvent pas être représentées en 8 bits
     * @return différences des valeur 8 bits données + fanions
     */
    public static int sub(int l, int r) {
        checkBits8(l);
        checkBits8(r);
        return sub(l, r, false);
    }


    /**
     * ajuste la valeur 8 bits données afin qu'elle soit au format DCB
     * @param v: valeur 8 bits
     * @param n: fanion n 
     * @param h: fanion h
     * @param c: fanion c
     * @throws: IllegalArgumentException si la valeur ne peut pas être représentée en 8 bits
     * @return valeur 8 bit en format DCB + fanions
     */
    public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {
        checkBits8(v);
        boolean fixL = h || (!n && (Bits.extract(v, 0, 4) > 9));
        boolean fixH = c || (!n && v > 0x99);
        int fixLInt = (fixL) ? 1 : 0;
        int fixHInt = (fixH) ? 1 : 0;
        int fix = (0x60 * fixHInt) + (0x06 * fixLInt);
        int value = (n) ? (v - fix) : (v + fix);
        value = Bits.clip(LENGTH_8, value);

        return packValueZNHC(value, value == 0, n, false , fixH);
    }

    /**
     * Retourne le & bit à bit des deux valeurs 8 bits données
     * Fanions: Z010
     * @param l: valeur 8 bits
     * @param r: valeur 8 bits
     * @throws: IllegalArgumentException si les valeurs ne peuvent pas être représentées en 8 bits
     * @return & des 2 valeurs 8 bits + fanions
     */
    public static int and(int l, int r) {
        checkBits8(l);
        checkBits8(r);
        return packValueZNHC((l & r), ((l & r) == 0), false, true, false);
    }

    /**
     * retourne le | des deux valeurs 8 bits données
     * Fanions: Z000
     * @param l: valeur 8 bits
     * @param r: valeur 8 bits
     * @throws: IllegalArgumentException si les valeurs ne peuvent pas être représentées en 8 bits
     * @return | des deux valeurs 8 bits données + fanions
     */
    public static int or(int l, int r) {
        checkBits8(l);
        checkBits8(r);
        return packValueZNHC((l | r), ((l | r) == 0), false, false, false);
    }

    /**
     * retourne le ^ des deux valeurs 8 bits données
     * Fanions: Z000
     * @param l: valeur 8 bits
     * @param r: valeur 8 bits
     * @throws: IllegalArgumentException si les valeurs ne peuvent pas être représentées en 8 bits
     * @return ^ des deux valeurs 8 bits données + fanions
     */
    public static int xor(int l, int r) {
        checkBits8(l);
        checkBits8(r);
        return packValueZNHC((l ^ r), ((l ^ r) == 0), false, false, false);
    }

    /**
     * Retourne la valeur 8 bits donnée décalée à gauche d'un bit
     * Fanions: Z00C, C contient le bit éjecté par le décalage
     * @param v: valeur 8 bits
     * @throws IllegalArgumentException si la valeur ne peut pas être représentée en 8 bits
     * @return v décalée à gauche d'un bit + fanions
     */
    public static int shiftLeft(int v) {
        checkBits8(v);
        int value = Bits.clip(LENGTH_8, (v << 1));
        boolean z = (value == 0);
        boolean c = Bits.test(v, BIT_8);
        return packValueZNHC(value, z, false, false, c);
    }

    /**
     * Retourne la valeur 8 bits donnée décalée arithmétiquement à droite d'un bit
     * Fanions: Z00C, C contient le bit éjecté par le décalage
     * @param v: valeur 8 bits
     * @throws IllegalArgumentException si la valeur ne peut pas être représentée en 8 bits
     * @return v décalée arithmétiquement à droite d'un bit + fanions
     */
    public static int shiftRightA(int v) {
        checkBits8(v);
        int value = v >>> 1;
        value = Bits.test(value, BIT_7) ? Bits.set(value, BIT_8, true) : value;
        boolean z = (value == 0);
        boolean c = Bits.test(v, 0);
        return packValueZNHC(value, z, false, false, c);
    }

    /**
     * Retourne la valeur 8 bits donnée décalée logiquement à droite d'un bit
     * Fanions: Z00C, C contient le bit éjecté par le décalage
     * @param v: valeur 8 bits
     * @throws IllegalArgumentException si la valeur ne peut pas être représentée en 8 bits
     * @return v décalée logiquement à droite d'un bit + fanions
     */
    public static int shiftRightL(int v) {
        checkBits8(v);
        int value = v >>> 1;
        boolean z = (value == 0);
        boolean c = Bits.test(v, 0);
        return packValueZNHC(value, z, false, false, c);
    }

    /**
     * Retourne la valeur 8 bits donnée après une rotation d'un bit
     * Fanions: Z00C, C contient le bit qui est passée d'une extrémité à l'autre lors de la rotation
     * @param d: direction de la rotation
     * @param v: valeur 8 bits
     * @throws IllegalArgumentException si la valeur ne peut pas être représentée en 8 bits
     * @return v décalée logiquement à droite d'un bit + fanions
     */
    public static int rotate(RotDir d, int v) {
        checkBits8(v);
        int value = Bits.rotate(LENGTH_8, v, rotMove(d));
        boolean z = (Bits.clip(LENGTH_8, value) == 0);
        boolean c = Bits.test(v, bitShiftedByRotMove(d));
        return packValueZNHC(value, z, false, false, c);
    } 

    /**
     * Retourne la rotation à travers la retenue, dans la direction donnée, de la combinaison de la valeur 8 bits et du 
     * fanion de retenue donnés, c'est-à-dire une rotation de la valeur de 8 bits et un 9eme bits selon le boolean
     * Fanions: Z00C, où C contient le bit de poids le plus fort qui compte comme la nouvelle retenue
     * @param d: direction de la rotation
     * @param v: valeur 8 bits
     * @param c: boolean indiquant la valeur du carry
     * @throws IllegalArgumentException si la valeur ne peut pas être représentée en 8 bits
     * @return la concaténation de v et du carry après rotation
     */
    public static int rotate(RotDir d, int v, boolean c) {
        checkBits8(v);
        int valueShifted = Bits.rotate(LENGTH_9, rotateConcatenation(v, c), rotMove(d));
        int valueClipped = Bits.clip(LENGTH_8, valueShifted);
        boolean z = (Bits.clip(LENGTH_8, valueClipped) == 0);
        boolean cBool = Bits.test(valueShifted, BIT_9);
        return packValueZNHC(valueClipped, z, false, false, cBool);
    }

    /**Echange les 4 bits de poids faibles et ceux de poids fort
     * Fanions: Z000 
     * @param v: valeur 8 bits
     * @throws IllegalArgumentException si la valeur ne peut pas être représenté en 8 bits
     * @return l'entier passsé en argument où ses 4 bits de poids fort et ses 4 bits de poids faible
     */
    public static int swap(int v) {
        checkBits8(v);
        int weakCopy = Bits.extract(v, 0, LENGTH_4);
        int strongCopy = Bits.extract(v, BIT_5, LENGTH_4);
        return packValueZNHC((weakCopy << LENGTH_4) + strongCopy, v == 0, false, false, false);
    }

    /**
     * Retourne la valeur 0 avec les fanions 
     * Fanions: Z010, Z vrai ssi le bit d'index donné vaut 0
     * @param v: valeur 8 bits
     * @param bitIndex: index du bit à tester
     * @throws IllegalArgumentException si la valeur ne peut pas être représenté en 8 bits
     * @throws IndexOutOfBoundsException si l'index n'est pas compris entre 0 et 7
     * @return la valeur 0 avec les fanions, où z représente la négation de 
     */
    public static int testBit(int v, int bitIndex) {    
        checkBits8(v);
        Objects.checkIndex(bitIndex, LENGTH_8);
        boolean z = Bits.test(v, bitIndex);
        return packValueZNHC(0, !z, false, true, false);
    }

    
    //Crée un pack valeur/fanion
    private static int packValueZNHC(int v, boolean z, boolean n, boolean h, boolean c) {
        return ((v << LENGTH_8) + maskZNHC(z, n, h, c));
    }

    //Permet de définir la direction de rotation
    private static int rotMove(RotDir d) {
        return (d.ordinal() == 0) ? 1 : -1;
    }

    //Permet d'obtenir le bit shifté par la rotation
    private static int bitShiftedByRotMove(RotDir d) {
        return (d.ordinal() == 0) ? BIT_8 : BIT_1;
    }

    //Permet de créer une nouvelle valeur 9 bits avec une valeur 8 bits et un boolean représentant le 9eme bit
    private static int rotateConcatenation(int v, boolean c) {
        return c ? Bits.set(v, BIT_9, true) : v;
    }


}
