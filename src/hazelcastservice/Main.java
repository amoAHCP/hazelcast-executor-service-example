/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hazelcastservice;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Daryl
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static final String SERVICE_NAME = "spinnerella";
    public static final int NUM_INSTANCES = 5;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("hazelcast.logging.type", "slf4j");
        List<HazelcastInstance> instances = new ArrayList<>(NUM_INSTANCES);
        for(int i = 0; i < NUM_INSTANCES; i++) {
            instances.add(Hazelcast.newHazelcastInstance());
            logger.info("instance {} up", i);
        }

        IExecutorService spinner = instances.get(0).getExecutorService(SERVICE_NAME);
        try {
            HazelcastIExecutorServiceExamples.TO_SOME_MEMBER.example(instances, spinner);
            HazelcastIExecutorServiceExamples.TO_PARTICULAR_MEMBER.example(instances, spinner);
            HazelcastIExecutorServiceExamples.ON_THE_KEY_OWNER.example(instances, spinner);
            HazelcastIExecutorServiceExamples.ON_A_SET_OF_MEMBERS.example(instances, spinner);
            HazelcastIExecutorServiceExamples.ON_ALL_MEMBERS.example(instances, spinner);
            HazelcastIExecutorServiceExamples.CALLBACK.example(instances, spinner);
            HazelcastIExecutorServiceExamples.MULTIPLE_MEMBERS_WITH_CALLBACK.example(instances, spinner);
            
            //Lets setup a loop to make sure they are all done (Especially the callback ones)
            for(HazelcastIExecutorServiceExamples example: HazelcastIExecutorServiceExamples.values()) {
                while(!example.isDone()) {
                    Thread.sleep(1000);
                }
            }
        } catch(ExecutionException ee) {
            logger.warn("Can't finish the job", ee);
        } catch(InterruptedException ie) {
            logger.warn("Everybody out of the pool", ie);
        } finally {
            // time to clean up my toys
            boolean allClear = false;
            
            while(!allClear) {
                try {
                    Thread.sleep(1000);
                    Hazelcast.shutdownAll();
                    allClear = true;
                } catch(InterruptedException ie) {
                    //got interrupted. try again
                } catch(RejectedExecutionException ree) {
                    logger.debug("caught a RejectedExecutionException");
                    allClear = false;
                }
            }
            
            logger.info("All done");
        }
    }
}
