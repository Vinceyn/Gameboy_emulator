package ch.epfl.gameboj.component.cartridge;
import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;
/**
 * Classe chargée de représenter une cartouche
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public final class Cartridge implements Component {
	private final Component component;
	private static final int CARTRIDGE_TYPE_ADDRESS = 0x147;
	private static final int[] RAM_SIZE = {0, 2048, 8192, 32768};
	private static final int RAM_SIZE_BYTE = 0x149;
	private static final int NB_OF_MBC1_ROM_TYPE = 3;
	
	/**
 	* Construit une cartouche contenant un contrôleur et la mémoire morte qui lui est attachée.
 	* Ne lance pas de NullPointerException car cela est fait dans mbc0.
 	* @param component : un controleur de banque mémoire
 	*/
	private Cartridge(Component component) {
    	this.component = component;
	}
	
	/**
 	* Retourne une cartouche dont la mémoire morte contient les octets du fichier donné. 
 	* @param romFile : le fichier dont on tire les octets
 	* @throws IOException en cas d'erreur d'entrée-sortie, ou si le fichier donné n'existe pas
 	* @throws IllegalArgumentException si le fichier en question ne contient pas 0 à la position 0x147
 	* @return une cartouche dont la mémoire morte contient les octets du fichier donné.
 	*/
	public static Cartridge ofFile(File romFile) throws IOException {
    	byte[] romBytes;
    	if(romFile.equals(null)) {
        	throw new IOException();
    	}
    	try(InputStream is = new FileInputStream(romFile)) {
        	romBytes = new byte[(int) romFile.length()];
        	romBytes = is.readAllBytes();
    	}
    	Rom rom = new Rom(romBytes);
    	int romType = romBytes[CARTRIDGE_TYPE_ADDRESS];
    	Component mbc;
    	if (romType == 0)
        	mbc = new MBC0(rom);
    	else if (romType <= NB_OF_MBC1_ROM_TYPE && romType > 0) {
    	    int ramSizeValue = romBytes[RAM_SIZE_BYTE];
        	mbc = new MBC1(rom, RAM_SIZE[ramSizeValue]);
    	}
    	else
        	throw new IllegalArgumentException();
    	return new Cartridge(mbc);
	}
	/* (non-Javadoc)
 	* @see ch.epfl.gameboj.component.Component#read(int)
 	*/
	@Override
	public int read(int address) {
    	checkBits16(address);
    	return component.read(address);
	}
	
	
	/* (non-Javadoc)
 	* @see ch.epfl.gamebo&j.component.Component#write(int, int)
 	*/
	@Override
	public void write(int address, int data) {
    	checkBits16(address);
    	checkBits8(data);
    	component.write(address, data);
	}
}

