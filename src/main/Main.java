package main;

import atlantis.Atlantis;
import atlantis.AtlantisIgniter;
import atlantis.combat.ANeatManager;
import atlantis.keyboard.AKeyboard;
import atlantis.util.ProcessHelper;
import java.math.BigInteger;
import java.util.Vector;
import jneat.*;

/**
 * This is the main class of the bot. Here everything starts.
 *
 * "A journey of a thousand miles begins with a single step." - Lao Tse
 */
public class Main {

    /**
     * Sets up Atlantis config and runs the bot.
     */
    public static void main(String[] args) {
        
         // Kill previous Starcraft.exe process
         ProcessHelper.killStarcraftProcess();
         // Kill previous Chaoslauncher.exe process
         ProcessHelper.killChaosLauncherProcess();
         // Dynamically modify bwapi.ini file, change race/enemy race etc
         // To change the race/enemy race, edit AtlantisIgniter constants
         AtlantisIgniter.modifyBwapiFileIfNeeded();
         // Autostart Chaoslauncher
         // Combined with Chaoslauncher -> Settings -> Run Starcraft on Startup 
         // SC will be autostarted at this moment
         ProcessHelper.startChaosLauncherProcess();
         // =============================================================
         // =============================================================
         // ==== See AtlantisConfig class to customize execution ========
         // =============================================================
         // =============================================================
         //
         // Create Atlantis object to use for this bot. It wraps BWMirror functionality.
         Atlantis atlantis = new Atlantis();
         // Listen for keyboard events
         AKeyboard.listenForKeyEvents();
         // Starts bot.
         atlantis.run();
         
         
        
        
       // Neat.readParam("asd");
        
       /* Neat.initbase();
        System.out.println(Neat.p_pop_size);
        
        
        Population neat = new Population(20 , 7  , 2 , 5, true , 0.5 );
        
        neat.print_to_filename("redes/generacion1.txt");*/
       // Population neat = new Population();
        //Population neat = new Population("redes/generacion1.txt");
        
        
       /* Vector<Organism> organismos = neat.organisms;
        for(int i = 0; i < organismos.size() ; i++){
            organismos.get(i).setFitness(i);
            System.out.println(
            organismos.get(i).getFitness());  
            if(i > 5){
            organismos.get(i).setWinner(true);}
            
        }*/
      //neat.epoch(1);
         
        
    }

}
