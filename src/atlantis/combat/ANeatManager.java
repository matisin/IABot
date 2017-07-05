package atlantis.combat;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.units.AUnit;
import java.util.HashMap;
import jneat.Neat;
import jneat.Network;
import jneat.Organism;
import jneat.Population;
import atlantis.units.Select;
import bwapi.Color;
import bwapi.Position;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import jneat.NNode;

public class ANeatManager {

    private static final ANeatManager INSTANCE = new ANeatManager();
    private static Population neat;
    private static int IndexLastBrain;
    private static boolean vuelta;
    private static HashMap<AUnit, Organism> Organismos;
    private static HashMap<AUnit, Integer> DamageDealt;
    private static HashMap<AUnit, Integer> InitialHitPoint;

    private ANeatManager() {
    }

    public static ANeatManager getInstance() {

        return INSTANCE;
    }

    public static void initANeatManager() {
        Organismos = new HashMap();
        DamageDealt = new HashMap();
        InitialHitPoint = new HashMap();
        Neat.initbase();
        neat = new Population("redes_nuevas/generacion" + AtlantisConfig.GENERACION_RED + ".txt");
        IndexLastBrain = 0;
        vuelta = false;

    }

    public static void saveGeneration() {
        neat.print_to_filename("redes_nuevas/generacion" + AtlantisConfig.GENERACION_RED + ".txt");
        try {
            File file = new File("resultados.txt");
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            Vector<Organism> organisms = neat.organisms;
            double total_fitness=0;
            double highest = neat.getHighest_fitness();
            for(int i = 0; i < organisms.size() ; i++){
                Organism organism = organisms.get(i);
                if(organism.getFitness() == highest){
                    organism.setChampion(true);
                }
                total_fitness+=organism.getFitness();
            }
            total_fitness = total_fitness / organisms.size();
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);            
            String resultado = AtlantisConfig.GENERACION_RED + " " + neat.getHighest_fitness() + " " + total_fitness + "\n";
            bw.write(resultado);
            bw.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setBrain(AUnit unit) {

        if (++IndexLastBrain > 19) {
            IndexLastBrain = 0;
            vuelta = true;
        }
        Organism organism = (Organism) neat.getOrganisms().get(IndexLastBrain);
        Organismos.put(unit, organism);
        if(!InitialHitPoint.containsKey(unit)){
            InitialHitPoint.put(unit, unit.getHitPoints());
        }        
        DamageDealt.put(unit, 0);
    }

    public static void damageDealt(AUnit unit, int damage) {
        int damage_before = DamageDealt.get(unit);
        DamageDealt.replace(unit, damage_before + damage);
    }

    public static void setFitnessOrEvolve() {
        Set<AUnit> units = Organismos.keySet();
        boolean faltan = false;
        for (AUnit unit : units) {
            Organism organism = Organismos.get(unit);
            double fitness = ((DamageDealt.get(unit) - InitialHitPoint.get(unit) + unit.getHitPoints()) / InitialHitPoint.get(unit).doubleValue()) + 1;
            
            if (organism.getFitness() < fitness) {
                organism.setFitness(fitness);
            }
        }
        if (vuelta) {
            AGame.sendMessage("Evolucion generacion " + (AtlantisConfig.GENERACION_RED + 1));
            vuelta = false;
            IndexLastBrain = 0;
            //saveGeneration();
            neat.epoch(AtlantisConfig.GENERACION_RED++); // Evolve the population and increment the generation.
        }
        List<AUnit> newUnits = Select.ourCombatUnits().listUnits();
        Organismos.clear();
        DamageDealt.clear();
        InitialHitPoint.clear();
        for (AUnit unit : newUnits) {
            setBrain(unit);
        }

    }

    public static boolean getAction(AUnit unit, AUnit enemyUnit) {
        Organismos.get(unit);
        Network brain = Organismos.get(unit).getNet();
        // AUnit enemyUnit = AEnemyTargeting.defineBestEnemyToAttackFor(unit);
        if (enemyUnit == null) {
            //no hay unidad que atacar
            return false;
        }
        //inputs para la red
        double inputs[] = new double[7];
        //Bias
        inputs[0] = -1.0;
        if (enemyUnit.isAirUnit() && unit.canAttackAirUnits()) {
            inputs[1] = unit.getAirWeaponCooldown();
            inputs[3] = unit.getWeaponRangeAir();
            inputs[4] = enemyUnit.getWeaponRangeAir();
        } else if (enemyUnit.isGroundUnit() && unit.canAttackGroundUnits()) {
            inputs[1] = unit.getGroundWeaponCooldown();
            inputs[3] = unit.getWeaponRangeGround();
            inputs[4] = enemyUnit.getWeaponRangeGround();
        } else {
            //no debería pasar
            return false;
        }
        inputs[2] = unit.getHP();
        Select<AUnit> enemySelector = Select.enemy();
        Select<AUnit> alliesSelector = Select.ourCombatUnits();
        inputs[5] = enemySelector.canAttack(unit).count();
        inputs[6] = alliesSelector.canAttack(enemyUnit).count();
        // FIN INPUTS

        //carga de inputs en la red
        brain.load_sensors(inputs);

        int net_depth = brain.max_depth();
        // first activate from sensor to next layer....
        brain.activate();

        // next activate each layer until the last level is reached
        for (int relax = 0; relax <= net_depth; relax++) {
            brain.activate();
        }

        // Retrieve outputs from the final layer.
        double correr = ((NNode) brain.getOutputs().elementAt(0)).getActivation();
        double pelear = ((NNode) brain.getOutputs().elementAt(1)).getActivation();
        if (correr > pelear) {
            return unit.runFrom(enemyUnit);
        } else {
            return unit.attackUnit(enemyUnit);
        }

    }
}
