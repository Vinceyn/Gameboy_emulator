package ch.epfl.gameboj.component.lcd;
import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;
import java.util.Arrays;
import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

/**
 * Classe représentant le contrôleur LCD de la gameboy
 * @author Vincent Yuan (287639)
 * @author Niels Escarfail (282347)
 */
public final class LcdController implements Component, Clocked {
    private static final int LCD_WIDTH = 160;
    private static final int LCD_HEIGHT = 144;
    private static final int SAVE_STAT_BITS = 0b00000111;
    private static final int TAKE_DATA_BITS = 0b11111000;
    private static final int CYCLES_IN_IMAGE = 17556;
    private static final int CYCLES_IN_A_LINE = 114;
    private static final int LINE_IN_IMAGE = 144;
    private static final int CYCLE_WHEN_MODE_2 = 0;
    private static final int CYCLES_IN_MODE_2 = 20;
    private static final int CYCLES_WHEN_MODE_3 = 20;
    private static final int CYCLES_IN_MODE_3 = 43;
    private static final int CYCLES_WHEN_MODE_0 = 63;
    private static final int CYCLES_IN_MODE_0 = 51;
    private static final int FULL_LINE_WIDTH = 256;
    private static final int WX_SHIFT = 7;
    private static final int TILES_IN_BG = 32;
    private static final int TILES_IN_WINDOW = 20;
    private static final int LENGTH_8 = 8;
    private static final int TILE_INDEX_CHOKEPOINT = 0x80;
    private static final int TILE_INDEX_SHIFT = 0x800;
    private static final int BYTES_IN_TILE = 0x10;
    private static final int LINES_IN_TILE = 8;
    private static final int BYTES_IN_LINE = 2;
    private static final int SPRITE_X_SHIFT = 8;
    private static final int SPRITE_Y_SHIFT = 16;
    private static final int SIZE_16 = 16;
    private static final int SIZE_8 = 8;
    private static final int SPRITES_IN_OAM = 40;
    private static final int MAX_SPRITES_IN_LINE = 10;
    private static final int BYTES_IN_SPRITE = 4;

    private final Cpu cpu;
    private final Ram videoRam;
    private final RamController videoRamController;
    private final RegisterFile<Register> lcdRegFile;
    private final Ram oamRam;
    private long nextNonIdleCycle;
    private long lcdOnCycle;
    private LcdImage.Builder nextImageBuilder;
    private LcdImage currentImage;
    private Bus bus;
    private int currentCopyAddress;
    private boolean copy;
    private int winY;


    /**
     * Registre de la gameboy 
     */
    private enum LCDRegs implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX;
    }

    /**
     * Bits du registre LCDC
     */
    private enum LCDCBits implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    }

    /**
     * Bits du registre STAT
     */
    private enum STATBits implements Bit {
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC;
    }

    /**
     * Modes du contrôleur LCD
     */
    private enum Mode implements Bit {
        horizontalBlank, verticalBlank, sprite, spriteAndGraphic
    }

    /**
     * Octets des caractéristiques des sprites
     *
     */
    private enum SpriteBytes implements Register {
        SPR_Y, SPR_X, SPR_INDEX, ATTR
    }

    /**
     * Bits des octets des sprites
     *
     */
    private enum SpriteAtt implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, PALETTE, FLIP_H, FLIP_V, BEHIND_BG;
    }
  
    /**
     * Constructeur du LCDController, initialise les attributs du lcdController
     * @param cpu: processeur associé au lcdControlleur
     */
    public LcdController(Cpu cpu) {
        this.cpu = cpu;
        lcdRegFile = new RegisterFile<>(LCDRegs.values());
        for(LCDRegs r : LCDRegs.values()) {
            lcdRegFile.set(r, 0);
        }
        videoRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
        winY = 0;
        currentImage = newImageBuilder().build();
        nextNonIdleCycle = Long.MAX_VALUE;
        nextImageBuilder = newImageBuilder();
        videoRamController = new RamController(videoRam, AddressMap.VIDEO_RAM_START, AddressMap.VIDEO_RAM_END);
        oamRam = new Ram(AddressMap.OAM_RAM_SIZE);
    }


    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        checkBits16(address);
        if (addressPointsInRegister(address)) { 	
            return lcdRegFile.get(getRegFromAddress(address));
        }
        if (addressPointsInOAM(address)) {
            return oamRam.read(address - AddressMap.OAM_START); 
        }
        return videoRamController.read(address);  	
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        checkBits16(address);
        checkBits8(data);
        if (addressPointsInRegister(address)) { 	
            switch (getRegFromAddress(address)) {      
            case LCDC :{
                setReg(LCDRegs.LCDC, data);
                if (!screenON()) {
                    setMode(Mode.horizontalBlank);
                    setReg(LCDRegs.LY, 0);
                    nextNonIdleCycle = Long.MAX_VALUE;
                }
            } break;
            case STAT: {
                int newData = (getReg(LCDRegs.STAT) & SAVE_STAT_BITS) | (data & TAKE_DATA_BITS);
                setReg(LCDRegs.STAT, newData);
            } break;
            case LY :{ 
            } break;
            case LYC : {
                changeLYOrLYCThenCheck(data, false);
            } break;
            case DMA : {
                setReg(LCDRegs.DMA, data);
                copy = true;
                currentCopyAddress = 0;
            }
            default :{
                setReg(getRegFromAddress(address), data);
            }
            }
        }
        if (addressPointsInOAM(address)) {
            oamRam.write(address - AddressMap.OAM_START, data);
        }
        videoRamController.write(address, data);
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    @Override
    public void cycle(long cycle) {
        if (screenON() && nextNonIdleCycle == Long.MAX_VALUE) {
            nextNonIdleCycle = cycle;
            lcdOnCycle = cycle;
        }
        if (cycle == nextNonIdleCycle) {
            reallyCycle(cycle);
        }
        if (copy) {
            if (currentCopyAddress < AddressMap.OAM_RAM_SIZE) {
                write(AddressMap.OAM_START + currentCopyAddress, bus.read(Bits.make16(getReg(LCDRegs.DMA), currentCopyAddress++)));
            }
            else {
                copy = false;
            }
        }
    }


    /**
     * Accès à l'image actuelle générée par le controlleur LCD
     * @return L'image actuelle
     */
    public LcdImage currentImage() {
        return currentImage;
    }


    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#attachTo(ch.epfl.gameboj.Bus)
     */
    @Override
    public void attachTo(Bus bus) {
        this.bus = bus;
        Component.super.attachTo(bus);
    }

    /**
     * Méthode appelé seulement aux changements de mode, s'occupe de changer le mode, de lever les interruptions si besoin,
     *  de faire les opérations liés au mode comme la création d'image ou le calcul d'une ligne, et de faire en sorte 
     *  qu'elle sera appelé lors du prochain changement de cycle
     */
    private void reallyCycle(long cycle) {
        int cycleInScreen = (int)((cycle - lcdOnCycle) % CYCLES_IN_IMAGE);
        int cycleInLine = cycleInScreen % CYCLES_IN_A_LINE;
        int currentLine = cycleInScreen / CYCLES_IN_A_LINE;
        int endImageComputationCycle = CYCLES_IN_A_LINE * LINE_IN_IMAGE;
        /* Cas où l'écran n'a pas fini d'être calculé */
        if (cycleInScreen < endImageComputationCycle) {
            switch(cycleInLine) {
            case CYCLE_WHEN_MODE_2 : {
                changeLYOrLYCThenCheck(currentLine, true);
                setMode(Mode.sprite);
                checkLCD_STATInterrupt(STATBits.INT_MODE2);
                nextNonIdleCycle += CYCLES_IN_MODE_2;
                if (cycleInScreen == 0) {
                    nextImageBuilder = newImageBuilder();
                    winY = 0;
                }            	
            } break;
            case CYCLES_WHEN_MODE_3: {   	
                setMode(Mode.spriteAndGraphic);
                nextNonIdleCycle += CYCLES_IN_MODE_3;
                computeLine();
            } break;
            case CYCLES_WHEN_MODE_0: {
                setMode(Mode.horizontalBlank);
                checkLCD_STATInterrupt(STATBits.INT_MODE0);
                nextNonIdleCycle += CYCLES_IN_MODE_0;
            } break;
            }
        } else {
            if (cycleInScreen == endImageComputationCycle) {
                setMode(Mode.verticalBlank);
                currentImage = nextImageBuilder.build();
                cpu.requestInterrupt(Interrupt.VBLANK);
                checkLCD_STATInterrupt(STATBits.INT_MODE1);
            }
            changeLYOrLYCThenCheck(currentLine, true);
            nextNonIdleCycle += CYCLES_IN_A_LINE;
        }
    }

    /*
     * Crée un nouveau LcdImage.Builder Vide
     */
    private LcdImage.Builder newImageBuilder() {
        return new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
    }

    /*
     * Crée une ligne vide
     */
    private LcdImageLine emptyLine() {
        return new LcdImageLine.Builder(LCD_WIDTH).build();
    }


    /**
     * Test si l'adresse correspond à un registre
     */
    private boolean addressPointsInRegister(int address) {
        return AddressMap.REGS_LCDC_START <= address && address < AddressMap.REGS_LCDC_END;
    }

    /**
     * Test si l'adresse correspond à l'OAM
     */
    private boolean addressPointsInOAM(int address) {
        return AddressMap.OAM_START <= address && address < AddressMap.OAM_END;
    }


    /**
     * Met un bit du registre STAT à newVALUE
     */
    private void setSTATBit(STATBits bit, boolean newValue) {
        lcdRegFile.setBit(LCDRegs.STAT, bit, newValue);
    }

    /**
     * Test un bit de LCDC
     */
    private boolean testLCDCBit(LCDCBits bit) {
        return lcdRegFile.testBit(LCDRegs.LCDC, bit);
    }

    /**
     * Met un registre à la valeur indiquée
     */
    private void setReg(LCDRegs register, int value) {
        lcdRegFile.set(register, value);
    }

    /**
     * Donne la valeur d'un registre
     */
    private int getReg(LCDRegs register) {
        return lcdRegFile.get(register);
    }

    /*
     * Donne la valeur d'un registre à une adresse donnée
     */
    private LCDRegs getRegFromAddress(int address) {
        return LCDRegs.values()[address - AddressMap.REGS_LCDC_START];
    }

    /**
     * Modifie les bits de STAT afin qu'ils correspondent au mode souhaitée
     */
    private void setMode(Mode mode) {
        int modePosition = mode.index();
        setSTATBit(STATBits.MODE0, Bits.test(modePosition, 0));
        setSTATBit(STATBits.MODE1, Bits.test(modePosition, 1));
    }

    /*
     * Vérifie si l'écran est allumé
     */
    private boolean screenON() {
        return testLCDCBit(LCDCBits.LCD_STATUS);
    }

    /*
     * Change la valeur de LY ou LYC selon le boolean, puis vérifie si ils sont égaux.
     * Dans ce cas, il met le bit de STAT LYC_EQ_LY à vrai, puis vérifie s'il doit lancer une interruption
     */
    private void changeLYOrLYCThenCheck(int value, boolean isLYValue) {
        setReg(isLYValue ? LCDRegs.LY : LCDRegs.LYC, value);
        if (getReg(LCDRegs.LY) == getReg(LCDRegs.LYC)) {
            setSTATBit(STATBits.LYC_EQ_LY, true);
            checkLCD_STATInterrupt(STATBits.INT_LYC);	           
        }
        else {
            setSTATBit(STATBits.LYC_EQ_LY, false);
        }
    }

    /**
     * Vérifie la valeur d'un bit de stat, et, si il est vrai, lance une interruption
     */
    private void checkLCD_STATInterrupt(STATBits interruption) {
        if (lcdRegFile.testBit(LCDRegs.STAT, interruption))
            cpu.requestInterrupt(Cpu.Interrupt.LCD_STAT);
    }

    /**
     * Calcule la ligne actuelle et la rajoute dans le builder d'image
     */
    private void computeLine() {
        /*Déclaration des variables */

        /*L'index est trouvé à partir du registre LY, puis création des builder d'image pour le background et le window */
        int index = getReg(LCDRegs.LY);
        LcdImageLine.Builder bgLineBuilder = new LcdImageLine.Builder(FULL_LINE_WIDTH);
        LcdImageLine.Builder winLineBuilder = new LcdImageLine.Builder(LCD_WIDTH);

        /* On cherche quels sont l'origine des tuiles et de leur index */
        int tileOrigin = AddressMap.TILE_SOURCE[testLCDCBit(LCDCBits.TILE_SOURCE) ? 1 : 0];
        int bgTileStartAddress = getTileStartAddress(LCDCBits.BG_AREA);    
        int winTileStartAddress = getTileStartAddress(LCDCBits.WIN_AREA);

        /* Calcul des valeurs du windows et du background*/
        int shiftedY = (index + getReg(LCDRegs.SCY)) % FULL_LINE_WIDTH;
        int WX = Math.max(0, getReg(LCDRegs.WX) - WX_SHIFT);
        int WY = getReg(LCDRegs.WY);

        /* Savoir si le background et/ou le window sont visibles*/
        boolean bgVisible = testLCDCBit(LCDCBits.BG);
        boolean windowVisible = testLCDCBit(LCDCBits.WIN) && 0 <= WX && WX < LCD_WIDTH && WY <= index;

        /*Calcul des lignes, on calcule 32 tuiles pour le background et 20 pour le windows*/
        for (int i = 0; i < TILES_IN_BG; ++i) {
            if (bgVisible) {
                setTileLine(shiftedY, i, bgTileStartAddress, tileOrigin, bgLineBuilder);
            }
            if (windowVisible && i < TILES_IN_WINDOW) {
                setTileLine(winY, i, winTileStartAddress, tileOrigin, winLineBuilder);
            }
        }
        
        if (windowVisible)
            winY++;

        /* Calcul de la ligne mélangeant windows et background*/
        int colors = getReg(LCDRegs.BGP);
        LcdImageLine bgLine = computeBgLine(bgLineBuilder, bgVisible, colors);
        LcdImageLine winLine = windowVisible ? winLineBuilder.build().extractWrapped(0,LCD_WIDTH).mapColors(colors) : emptyLine();
        LcdImageLine windowAndBgLine = windowVisible ? bgLine.join(winLine.shift(WX), WX) : bgLine;


        /*Calcul des sprites si ils sont présent sur l'écran*/
        LcdImageLine finalLine;
        if (testLCDCBit(LCDCBits.OBJ)) {
            int[] sprites = spritesIntersectingLine(index);
            LcdImageLine fgSprites = emptyLine();
            LcdImageLine bgSprites = emptyLine(); 

            for(int i : sprites) {
                int spriteIndex = Bits.clip(LENGTH_8, i);
                if(testSpriteAttribut(spriteIndex, SpriteAtt.BEHIND_BG)) {
                    bgSprites = setSprites(spriteIndex).below(bgSprites);
                } else {
                    fgSprites = setSprites(spriteIndex).below(fgSprites);
                }
            }

            /* Assemblage des différentes couches de ligne*/
            BitVector bgOpacity = windowAndBgLine.opacity().not().and(bgSprites.opacity()).not();   	
            finalLine = bgSprites.below(windowAndBgLine, bgOpacity);
            finalLine = finalLine.below(fgSprites);
        }
        else {
            finalLine = windowAndBgLine;
        }

        /*Remplissage de la future image avec la ligne calculée*/
        nextImageBuilder.setLine(finalLine, index);
    }

    /* Donne l'adresse de départ des tuiles selon le LCDCBits*/
    private int getTileStartAddress(LCDCBits WinOrBg) {
        return AddressMap.BG_DISPLAY_DATA[testLCDCBit(WinOrBg) ? 1 : 0];
    }

    /* Cherche la valeur de la tuile actuelle en calculant l'adresse, puis la met à l'index donnée*/
    private void setTileLine(int y, int index, int tileStartAddress, int tileOrigin, LcdImageLine.Builder line) {
        int shiftPlage = 0;
        int tileIndex = read(tileStartAddress + ((y / LINES_IN_TILE) * TILES_IN_BG) + index);
        if (!testLCDCBit(LCDCBits.TILE_SOURCE)) 
            shiftPlage = (tileIndex < TILE_INDEX_CHOKEPOINT) ? TILE_INDEX_SHIFT : -TILE_INDEX_SHIFT;
        int address = (tileOrigin + shiftPlage) + (tileIndex) * BYTES_IN_TILE + (y % LINES_IN_TILE) * BYTES_IN_LINE;
        line.setBytes(index, readAndReverse(address + 1), readAndReverse(address));
    }


    /*Donne une ligne contenant le sprite d'index donné shifté pour être à sa position X */
    private LcdImageLine setSprites(int spriteIndex) {

        /* Calcul de l'index du sprite et de son adresse */
        int lineInSprite = getReg(LCDRegs.LY) - (getSpriteInfo(spriteIndex, SpriteBytes.SPR_Y) - SPRITE_Y_SHIFT);
        int size = getSpriteSize();
        int tileIndex = getSpriteInfo(spriteIndex, SpriteBytes.SPR_INDEX);
        int address =  AddressMap.TILE_SOURCE[1] + (tileIndex * BYTES_IN_TILE) + 
                (testSpriteAttribut(spriteIndex, SpriteAtt.FLIP_V) ? (size - 1 - lineInSprite) * BYTES_IN_LINE : lineInSprite * BYTES_IN_LINE) ;    	        

        /*Met le sprite dans la ligne */
        LcdImageLine.Builder spriteLineBuilder = new LcdImageLine.Builder(LCD_WIDTH);        
        LcdImageLine spriteLine;
        if (testSpriteAttribut(spriteIndex, SpriteAtt.FLIP_H))
            spriteLine = spriteLineBuilder.setBytes(0, read(address + 1), read(address)).build();
        else
            spriteLine = spriteLineBuilder.setBytes(0, readAndReverse(address + 1), readAndReverse(address)).build();

        /*Met le sprite à la bonne position et change sa couleur */
        int spriteColours = testSpriteAttribut(spriteIndex, SpriteAtt.PALETTE) ? getReg(LCDRegs.OBP1) : getReg(LCDRegs.OBP0);
        spriteLine = spriteLine.shift(getSpriteInfo(spriteIndex, SpriteBytes.SPR_X) - SPRITE_X_SHIFT).mapColors(spriteColours); 
        return spriteLine;
    }

    /*Donne la taille du sprite */
    private int getSpriteSize() {
        return testLCDCBit(LCDCBits.OBJ_SIZE) ? SIZE_16 : SIZE_8;
    }

    /* Calcule les sprites sur la ligne d'index donnée, les trie et renvoie un tableau */
    private int[] spritesIntersectingLine(int y) {
        int[] bestSprites = new int[MAX_SPRITES_IN_LINE];
        int index = 0;
        int size = getSpriteSize();
        for(int i = 0; i < SPRITES_IN_OAM && index < bestSprites.length; ++i ) {
            int spriteX = getSpriteInfo(i, SpriteBytes.SPR_X);
            int spriteY = getSpriteInfo(i, SpriteBytes.SPR_Y) - SPRITE_Y_SHIFT;
            if (y >= spriteY && y < spriteY + size) {
                bestSprites[index++] = Bits.make16(spriteX, i);
            }
        }
        Arrays.sort(bestSprites, 0, index);
        bestSprites = Arrays.copyOf(bestSprites, index);
        return bestSprites;
    }

    /*Lit en inversant les sprites à cette adresse */
    private int readAndReverse(int address) {
        return Bits.reverse8(read(address));
    }

    /*Calcule la ligne de background selon si elle est visible, et si oui, change sa couleur avec la palette donnée*/
    private LcdImageLine computeBgLine(LcdImageLine.Builder bgLineBuilder, boolean bgVisible, int colours) {
        return bgVisible ?
                bgLineBuilder.build().extractWrapped(getReg(LCDRegs.SCX), LCD_WIDTH).mapColors(colours) //RAJOUTER LE MAP COLORS (mais il bug :/ :'( )
                : emptyLine();
    }

    /*Test un attribut du sprite */
    private boolean testSpriteAttribut(int spriteIndex, SpriteAtt attribut) {
        return Bits.test(getSpriteInfo(spriteIndex, SpriteBytes.ATTR), attribut);
    }

    /*Donne l'information voulue d'un sprite d'index donnée */
    private int getSpriteInfo(int spriteIndex, SpriteBytes info) {
        return oamRam.read(spriteIndex * BYTES_IN_SPRITE + info.ordinal());
    }
}

