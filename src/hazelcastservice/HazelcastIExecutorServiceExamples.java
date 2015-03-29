/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hazelcastservice;

import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiExecutionCallback;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Daryl
 */
public enum HazelcastIExecutorServiceExamples {
    TO_SOME_MEMBER() {
        @Override
        public void example(List<HazelcastInstance> instances, IExecutorService spinner)
                throws ExecutionException, InterruptedException {
            logger.info("Submit to some member.");
            Future<Integer> howMany = spinner.submit(new MoveItMoveIt());
            logger.info("It moved it {} times", howMany.get());
            done = true;
        }
    },
    TO_PARTICULAR_MEMBER {
        @Override
        public void example(List<HazelcastInstance> instances, IExecutorService spinner)
                throws ExecutionException, InterruptedException {
            logger.info("Submit to a particular member.");
            Member member = getRandomMember(instances);
            logger.debug("member is {}", member);
            Future<Integer> howMany = spinner.submitToMember(new MoveItMoveIt(), member);
            logger.info("It moved it {} times.", howMany.get());
            done = true;
        }
        
        private Member getRandomMember(List<HazelcastInstance> instances) {
            Set<Member> members = instances.get(0).getCluster().getMembers();
            int i = 0;
            int max = new Random().nextInt(instances.size());
            Iterator<Member> iterator = members.iterator();
            Member member = iterator.next();
            while(iterator.hasNext() && (i < max)) {
                member = iterator.next();
                i++;
            }
            return member;
        }
    },
    ON_THE_KEY_OWNER {
        @Override
        public void example(List<HazelcastInstance> instances, IExecutorService spinner)
            throws ExecutionException, InterruptedException {
            logger.info("Send to the one owning the key");
            HazelcastInstance randomInstance = getRandomInstance(instances);
            IMap<Long, Boolean> map = randomInstance.getMap("default");
            Long one = 1L;
            map.put(one, Boolean.TRUE);
            
            Future<Integer> howMany = spinner.submitToKeyOwner(new MoveItMoveIt(), one);
            logger.info("It moved it {} times.", howMany.get());
            done = true;
        }
        
        private HazelcastInstance getRandomInstance(List<HazelcastInstance> instances) {
            return instances.get(new Random().nextInt(instances.size()));
        }

    },
    ON_A_SET_OF_MEMBERS {
        @Override
        public void example(List<HazelcastInstance> instances, IExecutorService spinner)
            throws ExecutionException, InterruptedException {
            logger.info("Send to some of the members");
            Set<Member> randomMembers = getRandomMembers(instances);
            Map<Member, Future<Integer>> results = 
                    spinner.submitToMembers(new MoveItMoveIt(), randomMembers);
            for(Future<Integer> howMany: results.values()) {
                logger.info("It moved {} times", howMany.get());
            }
            done = true;
        }
        
        private Set<Member> getRandomMembers(List<HazelcastInstance> instances) {
            int max = new Random().nextInt(instances.size());
            Set<Member> newSet = new HashSet<>(instances.size());
            int k = 0;
            Iterator<Member> i = instances.get(0).getCluster().getMembers().iterator();
            while(i.hasNext() && k < max) {
                newSet.add(i.next());
                k++;
            }
            return newSet;
        }
    },
    ON_ALL_MEMBERS {
        @Override
        public void example(List<HazelcastInstance> instances, IExecutorService spinner)
            throws ExecutionException, InterruptedException {
            logger.info("Send to all members");
            Map<Member, Future<Integer>> results = 
                    spinner.submitToAllMembers(new MoveItMoveIt());
            for(Future<Integer> howMany: results.values()) {
                logger.info("It moved {} times", howMany.get());
            }
            done = true;
        }
    },
    CALLBACK {
        @Override
        public void example(List<HazelcastInstance> instances, IExecutorService spinner)
            throws ExecutionException, InterruptedException {
            logger.info("example with a callback");
            spinner.submit(new MoveItMoveIt(), new ExecutionCallback<Integer>() {
                @Override
                public void onResponse(Integer response) {
                    logger.info("It moved {} times", response);
                    done = true;
                }

                @Override
                public void onFailure(Throwable thrwbl) {
                    logger.error("trouble in the callback", thrwbl);
                    done = true;
                }
            });
        }        
    },
    MULTIPLE_MEMBERS_WITH_CALLBACK {
        @Override
        public void example(List<HazelcastInstance> instances, IExecutorService spinner)
            throws ExecutionException, InterruptedException {
            logger.info("running on multiple members with callback");
            spinner.submitToAllMembers(new MoveItMoveIt(), new MultiExecutionCallback() {

                @Override
                public void onResponse(Member member, Object o) {
                    logger.info("member finished with {} moves", o);
                }

                @Override
                public void onComplete(Map<Member, Object> map) {
                    logger.info("All members completed");
                    for(Object value: map.values()) {
                        logger.info("It moved {} times", value);
                    }
                    done = true;
                }
            });
        }
    };

    
    
    private static final Logger logger = LoggerFactory.getLogger(HazelcastIExecutorServiceExamples.class);
    
    protected boolean done;
    
    private HazelcastIExecutorServiceExamples() {
        done = false;
    }
    
    public abstract void example(List<HazelcastInstance> instances, IExecutorService spinner)
            throws ExecutionException, InterruptedException;
    
    public boolean isDone() {
        return done;
    }
}
