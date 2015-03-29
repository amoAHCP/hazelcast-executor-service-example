/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hazelcastservice;

import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class was inspired by the song "I Like to Move it" from the movie 
 * Madagascar by Dreamworks.  I offer NO apologies for using it.  
 * 
 * To those software developers who like consistent results, I used java.util.Random to
 * make it loop inconsistently each time call is called.  
 * 
 * Sometimes you need to make your own entertainment.
 * @author Daryl
 */
public class MoveItMoveIt implements Callable<Integer>, Serializable {
    private static final Logger logger = LoggerFactory.getLogger(MoveItMoveIt.class);
    private static final int UPPER_BOUND = 15;
        
    @Override
    public Integer call() throws Exception {
        Random random = new Random();
        int howMany = random.nextInt(UPPER_BOUND);
//        int howMany = 2;
        for(int i = 0; i < howMany; i++) {
            logger.info("I like to Move it Move it!");
        }
        logger.info("Move it!");
        return howMany;
    }
}
