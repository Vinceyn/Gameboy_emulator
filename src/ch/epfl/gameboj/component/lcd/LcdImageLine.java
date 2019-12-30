package ch.epfl.gameboj.component.lcd;
import static ch.epfl.gameboj.Preconditions.checkArgument;
import static ch.epfl.gameboj.Preconditions.checkBits8;
import java.util.Objects;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
/**
 * Classe chargée de représenter une ligne de l'image LCD
 * @author Niels Escarfail (282347)
 * @author Vincent Yuan (287639)
 */
public final class LcdImageLine {
    private static final int NB_OF_COLORS = 4;
    private static final int NORMAL_PALETTE = 0b11_10_01_00;
    private static final int BITS_IN_A_COLOR = 2;
    
	private final BitVector msb;
	private final BitVector lsb;
	private final BitVector opacity;
	
	/**
 	* Construit une instance de LcdImageLine
 	* @param msb : BitVector représentant les MSB
 	* @param lsb : BitVector représentant les LSB
 	* @param opacity : BitVector représentant l'opacité
 	* @throws IllegalArgumentException si les BitVector n'ont pas la même taille
 	*/
	public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
    	checkArgument(msb.size() == lsb.size() && msb.size() == opacity.size());
    	this.msb = msb;
    	this.lsb = lsb;
    	this.opacity = opacity;
	}
	
	/**
 	* Retourne la longueur, en pixels, de la ligne
 	* @return la longueur, en pixels, de la ligne
 	*/
	public int size() {
    	return msb.size();
	}
	
	/**
 	* Retourne le vecteur des bits de poids fort
 	* @return le vecteur des bits de poids fort
 	*/
	public BitVector msb() {
    	return msb;
	}
	
	/**
 	* Retourne le vecteur des bits de poids faible
 	* @return le vecteur des bits de poids faible
 	*/
	public BitVector lsb() {
    	return lsb;
	}
	
	/**
 	* Retourne le vecteur de l'opacité
 	* @return le vecteur de l'opacité
 	*/
	public BitVector opacity() {
    	return opacity;
	}
	
	/**
 	* Décale la ligne d'un nombre de pixels donné, en préservant sa longueur
 	* @param decalage nombre de pixels à décaler
 	* @return une nouvelle ligne, décaler du nombre de pixel donnée
 	*/
	public LcdImageLine shift(int decalage) {
    	return new LcdImageLine(msb.shift(decalage), lsb.shift(decalage), opacity.shift(decalage));
	}
	
	
	/**
 	* Extrait de l'extension infinie par enroulement, à partir d'un pixel donné, une ligne de longueur donnée
 	* @param index: pixel à partir duquel on extrait l'extension
 	* @param length: longueur de l'extension
 	* @throws IllegalArgumentException si la longueur est négative ou null
 	* @return une nouvelle ligne après extension infinie par enroulement
 	*/
	public LcdImageLine extractWrapped(int index, int length) {
    	checkArgument(length > 0);
    	return new LcdImageLine(msb.extractWrapped(index, length), lsb.extractWrapped(index, length), opacity.extractWrapped(index, length));
	}

	/**
 	* Transforme les couleurs de la ligne en fonction d'une palette
 	* @param encodedByte: palette à partir duquel on opère les changements
 	* @throws IllegalArgumentException si la palette est invalide
 	* @return une nouvelle ligne, après channgement des couleurs de la palette
 	*/
	public LcdImageLine mapColors(int encodedByte) {
    	checkBits8(encodedByte);
    	if (encodedByte == NORMAL_PALETTE)
        	return this;
    	
    	else {
    	BitVector bitVectorMSB = new BitVector(size());
    	BitVector bitVectorLSB = new BitVector(size());
    	
    	for (int i = 0; i < NB_OF_COLORS; ++i) {        	
    	    
        	BitVector msbBitVector = Bits.test(i, 1) ? msb : msb.not();
        	BitVector LSBBitVector = Bits.test(i, 0) ? lsb : lsb.not();
        	BitVector color = msbBitVector.and(LSBBitVector);
        	        	
        	int nextColor = Bits.extract(encodedByte, BITS_IN_A_COLOR * i, BITS_IN_A_COLOR);
        	
        	if (Bits.test(nextColor, 0))
        	    bitVectorLSB = bitVectorLSB.or(color);
        	
        	if (Bits.test(nextColor, 1))
        	    bitVectorMSB = bitVectorMSB.or(color);
    	}
    	
    	return new LcdImageLine(bitVectorMSB, bitVectorLSB, opacity);
    	}
	}
	
	/**
 	* compose la ligne avec une seconde de même longueur, placée au-dessus d'elle,
 	* en utilisant l'opacité de la ligne supérieure pour effectuer la composition
 	* @param that: une autre ligne lcd
 	* @throws: NullPointerException si la ligne lcd passée en argument est nulle
 	* @throws IllegalArgumentException si les lignes n'ont pas la même longueur
 	* @return une nouvelle ligne, superposition de la ligne passée en argument sur la ligne actuelle
 	*/
	public LcdImageLine below(LcdImageLine that) {
    	return below(that, that.opacity());
	}
	
	/**
 	* Compose la ligne avec une seconde de même longueur, placée au-dessus d'elle, en utilisant un vecteur d'opacité
 	* @param that: autre ligne LCD
 	* @param opacity: vecteur d'opacité
 	* @throws NullPointerException si la ligne LCD ou le vecteur d'opacité est nul
 	* @throws IllegalArgumentException si les lignes de l'instance actuelle, du vecteur d'opacité et de la ligne n'ont pas la même longueur
 	* @return une nouvelle ligne, superposition de la ligne passée en argument et de la ligne actuelle selon le vecteur d'opacité
 	*/
	public LcdImageLine below(LcdImageLine that, BitVector opacity) {
    	Objects.requireNonNull(that);
    	Objects.requireNonNull(opacity);
    	checkArgument(that.size() == this.size() && opacity.size() == that.size());
    	BitVector bitVectorMSB = that.msb().and(opacity).or(msb.and(opacity.not()));
    	BitVector bitVectorLSB = that.lsb().and(opacity).or(lsb.and(opacity.not()));
    	BitVector bitVectorOpacity = opacity.or(this.opacity);
    	return new LcdImageLine(bitVectorMSB, bitVectorLSB, bitVectorOpacity);
	}
	
	/**
 	* Joint la ligne avec une autre de même longueur, à partir d'un pixel d'index donnée
 	* @param that: autre ligne LCD
 	* @param index: index à partir duquel on doit joindre l'autre ligne
 	* @return une nouvelle ligne, jointure entre celle de l'instance actuelle et celle passée en argument
 	* @throws NullPointerException si la ligne passée en argument est nulle
 	* @throws IllegalArgumentException ou si les vecteurs de la ligne passée en argument et de l'instance actuelle n'ont pas la même longueur
 	* @throws IndexOutOfBoundsException si l'index de la ligne est inférieur à 0 ou supérieur à la taille des vecteurs
 	*/
	public LcdImageLine join(LcdImageLine that, int index) {
    	Objects.requireNonNull(that);
    	checkArgument(that.size() == this.size());
    	Objects.checkIndex(index, size());
    	BitVector bitVectorMask = new BitVector(size(), true);
    	bitVectorMask = bitVectorMask.shift(index);
    	BitVector bitVectorMSB = that.msb.and(bitVectorMask).or(this.msb.and(bitVectorMask.not()));
    	BitVector bitVectorLSB = that.lsb.and(bitVectorMask).or(this.lsb.and(bitVectorMask.not()));
    	BitVector bitVectorOpacity = that.opacity.and(bitVectorMask).or(this.msb.and(bitVectorMask.not()));
    	return new LcdImageLine(bitVectorMSB, bitVectorLSB, bitVectorOpacity);
	}
	
	/* (non-Javadoc)
 	* @see java.lang.Object#equals(java.lang.Object)
 	*/
	@Override
	public boolean equals(Object o) {
    	return o instanceof LcdImageLine && ((LcdImageLine)o).lsb.equals(lsb) && ((LcdImageLine)o).msb.equals(msb)
            	&& ((LcdImageLine)o).opacity.equals(opacity);
	}
	
	/* (non-Javadoc)
 	* @see java.lang.Object#hashCode()
 	*/
	@Override
	public int hashCode() {
    	return Objects.hash(msb.hashCode(), lsb.hashCode(), opacity.hashCode());
	}
	
	/**
 	* Classe constructeur pour construire les instance de LcdImageLine
 	*/
	public static final class Builder {
    	private BitVector.Builder bitVectorBuilderMSB;
    	private BitVector.Builder bitVectorBuilderLSB;
    	
    	/**
     	* Constructeur de LcdImageLine.Builder, initialise les builder de vecteur de bit à une taille donnée
     	* @param size: taille des vecteurs de bits donnée
     	* @throws IllegalArgumentException si la taille n'est pas strictement positive
     	*/
    	public Builder(int size) {
        	checkArgument(size > 0);
        	bitVectorBuilderMSB = new BitVector.Builder(size);
        	bitVectorBuilderLSB = new BitVector.Builder(size);
    	}
    	
    	/**
     	* Définit la valeur des octets de poids forts et de poids faible de la ligne à partir d'un index donné
     	* @param index: index donné
     	* @param byteMSB: octet à ajouter dans MSB
     	* @param byteLSB: octet à ajouter dans LSB
     	* @return une nouvelle instance de LcdImageLine.Builder, avec les octets ajoutés à lindex indiqué
     	* @throws IllegalArgumentException si byteMSB ou byteLSB n'est pas un octet
     	* @throws IndexOutOfBoundsException si l'index n'est pas valide
     	*/
    	public LcdImageLine.Builder setBytes(int index, int byteMSB, int byteLSB) {
        	bitVectorBuilderMSB.setByte(index, byteMSB);
        	bitVectorBuilderLSB.setByte(index, byteLSB);
        	return this;
    	}
    	
    	/**
     	* Construit une instance de LcdImageLine à partir de cette instance du bâtisseur
     	* @return une instance de LcdImageLine
     	*/
    	public LcdImageLine build() {
        	BitVector bitVectorMSB = bitVectorBuilderMSB.build();
        	BitVector bitVectorLSB = bitVectorBuilderLSB.build();
        	BitVector bitVectorOpacity = bitVectorMSB.or(bitVectorLSB);
        	return new LcdImageLine(bitVectorMSB, bitVectorLSB, bitVectorOpacity);
    	}
	}
}


