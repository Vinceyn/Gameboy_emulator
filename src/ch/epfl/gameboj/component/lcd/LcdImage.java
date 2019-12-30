package ch.epfl.gameboj.component.lcd;
import static ch.epfl.gameboj.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * Classe chargée de représenter une image LCD
 * @author Niels Escarfail (282347)
 * @author Vincent Yuan (287639)
 */

public final class LcdImage {
	private final List<LcdImageLine> lineList; 
	private final int width;
	private final int height;
	
	/**
 	* Construit une Image LCD
 	* @param listOfLine: liste des lignes lcd d'image
 	* @param width: largeur de l'image
 	* @param height: hauteur de l'image
 	* @throws NullPointerException si la liste des lignes lcd est nulle
 	* @throws IllegalArgumentException si la largeur ou la hauteur est nulle ou inférieure à 0
 	*/
	public LcdImage(int width, int height, List<LcdImageLine> listOfLine) {
    	Objects.requireNonNull(listOfLine);
    	checkArgument(width > 0 && height > 0);
        checkArgument(height == listOfLine.size());
        checkArgument(width == listOfLine.get(0).size());
    	this.width = width;
    	this.height = height;
    	lineList = Collections.unmodifiableList(new ArrayList<>(listOfLine));
	}
	
	/**
 	* Retourne la largeur de l'image
 	* @return la largeur de l'image
 	*/
	public int width() {
    	return width;
	}
	
	/**
 	* Retourne la hauteur de l'image
 	* @return la hauteur de l'image
 	*/
	public int height() {
    	return height;
	}
	
	/**
 	* Obtient, sous la forme d'un entier compris entre 0 et 3, la couleur du pixel d'indice x, y;
 	* @param x: abscisse du pixel
 	* @param y: ordonnée du pixel
 	* @return la couleur du pixel d'abscisse x et d'ordonnée y
 	* @throws IllegalArgumentException si x ou y ne sont pas valides
 	*/
	public int get(int x, int y) {    	
	    checkArgument(x >= 0 && y >= 0 && x < width && y < height);
    	int bit1 = (lineList.get(y).msb().testBit(x) ? 1 : 0) << 1;
    	int bit0 = lineList.get(y).lsb().testBit(x) ? 1 : 0;
    	return bit1 | bit0;
	}
	
	
	
	/* (non-Javadoc)
 	* @see java.lang.Object#equals(java.lang.Object)
 	*/
	@Override
	public boolean equals(Object o) {  
    	return o instanceof LcdImage && Arrays.deepEquals(((LcdImage)o).lineList.toArray(), this.lineList.toArray());
	}

	/* (non-Javadoc)
 	* @see java.lang.Object#hashCode()
 	*/
	@Override
	public int hashCode() {
    	return Objects.hash(width, height, Objects.hashCode(lineList));
	}
	
	/**
 	* Classe chargée d'aider à la construction d'un LCDImage
 	*/
	public static final class Builder{
    	private final int width;
    	private final int height;
    	private ArrayList<LcdImageLine> listOfLine;
    	
    	/**
     	* Construit une instance de LcdImage.builder
     	* @param width: longueur du builder de l'image
     	* @param height: hauteur du builder de l'image
     	* @throws IllegalArgumentException si la hauteur ou la longueur est négative
     	*/
    	public Builder(int width, int height) {
        	checkArgument(width > 0  && height > 0);
        	listOfLine = new ArrayList<LcdImageLine>();
        	for (int i = 0; i < height; ++i) { 
        	     listOfLine.add(new LcdImageLine.Builder(width).build());
        	}
        	this.width = width;
        	this.height = height;
    	}
    	
    	/**
     	* change la ligne d'index donnée
     	* @param lineToAdd: ligne à ajouter
     	* @param index: index à ajouter
     	* @return une nouvelle instance de LcdImage.Builder, après ajout de la ligne
     	* @throws IllegalArgumentException si la taille de la ligne ne correspond pas avec la taille des lignes du builder
     	* @throws IndexOutOfBoundsException si l'index n'est pas valide
     	*/
    	public LcdImage.Builder setLine(LcdImageLine lineToAdd, int index) {
        	checkArgument(lineToAdd.size() == width);
        	if (index < 0)
            	throw new IndexOutOfBoundsException();
        	checkArgument(index < height);
        	listOfLine.set(index, lineToAdd);
        	return this;
    	}
    	
    	/**
     	* Construit un LcdImage à partir de l'instance
     	* @return une instance de LcdImage
     	*/
    	public LcdImage build() {
        	return new LcdImage(width, height, listOfLine);
    	}
    	
	}
}


