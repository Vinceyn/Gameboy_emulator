package ch.epfl.gameboj;
import static ch.epfl.gameboj.Preconditions.checkArgument;

//TODO 2 fois cpu attaché mdr
import java.util.Objects;

import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;
/**
 * Classe chargée de représenter une Game Boy
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public final class GameBoy {
	private final Bus bus;
	private final Cpu cpu;
	private final Timer timer;
    private final LcdController lcdController;
    private final Joypad joypad;
	private long actualCycle;
	
	
	public static final long cyclesPerSec = 0x100000;
	public static final double cyclesPerNanosec = cyclesPerSec / Math.pow(10, 9);
	
	
	/**
 	* Construit une GameBoy et instancie les différents composants de la console et de les attache à un bus commun.
 	* @param cartridge : la cartouche que l'on met dans la GameBoy.
 	* @throws NullPointerException si la cartouche donnée est nulle.
 	*/
	public GameBoy(Cartridge cartridge) {
    	Objects.requireNonNull(cartridge);
    	BootRomController bootRomController = new BootRomController(cartridge);    	
    	Bus bus = new Bus();
    	Ram workRam = new Ram(AddressMap.WORK_RAM_SIZE);
    	RamController workRamController = new RamController(workRam, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
    	RamController echoRamController = new RamController(workRam, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
    	cpu = new Cpu();
    	timer = new Timer(cpu);
        lcdController = new LcdController(cpu);
        joypad = new Joypad(cpu);
        
    	bus.attach(bootRomController);
    	bus.attach(workRamController);
    	bus.attach(echoRamController);
    	bus.attach(timer);
    	cpu.attachTo(bus);
    	lcdController.attachTo(bus);
    	joypad.attachTo(bus);
    	this.bus = bus;
	}
	
	
	/**
 	* Simule le fonctionnement du GameBoy jusqu'au cycle donné moins 1
 	* @param cycle : le nombre de cycles que la Gameboy doit effectuer
 	* @throws IllegalArgumentException si un nombre (strictement) supérieur de cycles a déjà été simulé
 	*/
	public void runUntil(long cycle) {
    	checkArgument(actualCycle <= cycle);
    	while (actualCycle < cycle) {
        	timer.cycle(actualCycle);
        	lcdController.cycle(actualCycle);
        	cpu.cycle(actualCycle);
        	++actualCycle;
    	}
	}
	
	/**
 	* Donne accès au minuteur
 	* @return timer : le minuteur
 	*/
	public Timer timer() {
    	return timer;
	}
	
	/**
 	* Retourne le nombre de cycles déjà simulés
 	* @return actualCycle : le nombre de cycles déjà simulés
 	*/
	public long cycles() {
    	return actualCycle;
	}
	
	/**
 	* Accesseur pour le bus
 	* @return bus : le bus de la Game Boy
 	*/
	public Bus bus() {
    	return bus;
	}
	
	/**
 	* Accesseur pour le processeur de la Game Boy
 	* @return cpu : le processeur de la Game Boy
 	*/
	public Cpu cpu() {
    	return cpu;
	}
	
	/**
	 * Accesseur pour le lcdController de la Game Boy
	 * @return lcdController : le controlleur d'ecran de la Game Boy
	 */
	public LcdController lcdController() {
	    return lcdController; 
	}
	
	/**
	 * Accesseur pour le joypad de la Game Boy
	 * @return joypad : le clavier de la Game Boy
	 */
	public Joypad joypad() {
	    return joypad;
	}
}


