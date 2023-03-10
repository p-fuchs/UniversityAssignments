package cp2022.solution;

import cp2022.base.Workshop;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

import cp2022.base.Workplace;
import cp2022.base.WorkplaceId;

public class SmartWorkshop implements Workshop {
    public static final boolean TESTING_MODE = false;

    private static class SmartWorkplace extends Workplace {
        private final SmartWorkshop handler;
        // Id from range 0..N-1. Where N is count of workshop's workplaces.
        private final int internalId;
        private final Workplace workplace;

        // Barrier to await cycle switch completion.
        private Optional<CyclicBarrier> cycleAwait;

        // Queue to enter this workplace from outside (not switch!).
        private Semaphore enterQueue;
        // Size of above queue.
        private int enterQueueSize;

        // Boolean states if some worker is sitting at this workplace.
        private boolean occupied;

        // Set of SmartWorkplaces from which workers want to perform switch to this workplace.
        private Set<SmartWorkplace> switchOrigins;

        // Simple switch = switch without cycle.
        // Boolean indicates whether security checks must be performed before use().
        private volatile boolean simpleSwitchChecked;
        // Boolean indicates whether worker is waiting for job permission.
        private volatile boolean waitingOnSimpleSwitch;
        // Boolean indicates if worker can start working at this workplace.
        private volatile boolean canWork;
        // Semaphore at which worker waits until he can work.
        private Semaphore simpleSwitchWaiting;
        // Field may contain reference to previously occupied workplace of
        // current workplace worker. It is used in switching process.
        private Optional<SmartWorkplace> pastWorkplace;

        // Semaphore at which current worker waits until he can
        // perform wanted change of workplace.
        private Semaphore switchingSemaphore;

        public SmartWorkplace(Workplace workplace, SmartWorkshop handler, int internalId) {
            super(workplace.getId());

            this.workplace = workplace;
            this.handler = handler;
            this.internalId = internalId;

            this.cycleAwait = Optional.empty();

            this.pastWorkplace = Optional.empty();
            this.simpleSwitchChecked = true;
            this.waitingOnSimpleSwitch = false;
            this.canWork = true;
            this.simpleSwitchWaiting = new Semaphore(0);

            this.enterQueue = new Semaphore(0, true);
            this.enterQueueSize = 0;

            this.switchOrigins = new LinkedHashSet<>();
            this.switchingSemaphore = new Semaphore(0);

            this.occupied = false;
        }

        // Worker wants to enter this workplace from outside of workshop.
        // Workshop's main mutex must be acquired in order to use this function.
        // Function does NOT consume main workshop's mutex.
        public void enter() throws InterruptedException {
            if (this.occupied) {
                this.enterQueueSize++;
                this.handler.workshopMutex().release();
                this.enterQueue.acquire();
                this.enterQueueSize--;
            }

            this.occupied = true;
        }

        // Function performs checks while worker leaves workplace (not in switching cycle).
        // Function DOES consume main workshop's mutex so it must be acquired before function call.
        private void leavingCheck() {
            if (!this.switchOrigins.isEmpty()) {
                if (TESTING_MODE) System.out.println("LeavingCheck - switchOrigins is not empty.");
                SmartWorkplace waiting = this.switchOrigins.iterator().next();
                waiting.switchingSemaphore.release();
            } else if (this.enterQueueSize > 0) {
                if (TESTING_MODE) System.out.println("Enter queue not empty");
                this.enterQueue.release();
            } else if (!this.handler.livelinessQueue().tryConsumeMainMutex()) {
                if (TESTING_MODE) System.out.println("Just relaxing mutex.");
                this.handler.workshopMutex().release();
            }
        }

        // Thread invoking function should be in possession of workshop's mainMutex
        // Function DOES consume main workshop's mutex. Represents situation where worker
        // of id userId wants to change site from this workplace to target.
        public void switchTo(SmartWorkplace target, Long userId) throws InterruptedException {
            if (!target.occupied) {
                // Target workplace is not occupied.
                this.occupied = false;
                target.occupied = true;

                this.simpleSwitchChecked = false;
                this.canWork = false;
                target.pastWorkplace = Optional.of(this);

                this.handler.reportSwitch(userId, target);

                this.leavingCheck();
            } else {
                // Target is occupied
                target.switchOrigins.add(this);
                Optional<Integer> possibleCycleLength = this.handler.workplaceGraph().processEdge(this.internalId, target.internalId);

                if (possibleCycleLength.isEmpty()) {
                    // No cycle created by this switch request
                    this.handler.workshopMutex().release();
                    this.switchingSemaphore.acquire();
                    // No we inherit the workshopMutex and target is ready (there can be cycle)
                    this.handler.workplaceGraph().removeEdge(this.internalId, target.internalId);
                    this.handler.reportSwitch(userId, target);
                    target.switchOrigins.remove(this);

                    if (this.cycleAwait.isPresent()) {
                        // We have a cycle
                        if (target.occupied) {
                            // Target did not start cycle removing process (because first worker which finds out about cycle
                            // sets his workplace's occupied status to false).
                            target.cycleAwait = this.cycleAwait;
                            target.occupied = true;

                            // Cycle message is spread.
                            target.switchingSemaphore.release();
                        } else {
                            // Target started cycle removing process, so we are last changing worker
                            target.occupied = true;

                            // Tries to handle mutex to liveliness condition guard.
                            if (!this.handler.livelinessQueue().tryConsumeMainMutex()) {
                                this.handler.workshopMutex().release();
                            }
                        }
                    } else {
                        // No cycle, just normal switch
                        target.occupied = true;
                        this.occupied = false;

                        // Synchronization after simple switch.
                        this.simpleSwitchChecked = false;
                        this.canWork = false;
                        target.pastWorkplace = Optional.of(this);

                        this.leavingCheck();
                    }
                } else {
                    // We are first worker, which removes cycle.
                    this.handler.reportSwitch(userId, target);
                    target.switchOrigins.remove(this);

                    // We are creating synchronization mechanism.
                    this.cycleAwait = Optional.of(new CyclicBarrier(possibleCycleLength.get()));
                    target.cycleAwait = this.cycleAwait;

                    // We are signaling that we are first worker.
                    this.occupied = false;
                    
                    // Cycle change message spread.
                    target.switchingSemaphore.release();
                }
            }
        }

        // Worker wants to leave workshop from this workplace.
        // Function DOES consume workshop's main mutex, so it must be
        // acquired before this function call.
        public void leave(Long userId) {
            this.handler.reportLeave(userId);
            this.cycleAwait = Optional.empty();
            this.occupied = false;

            // For prevention we reset workplace's status.
            this.simpleSwitchChecked = true;
            this.canWork = true;
            this.pastWorkplace = Optional.empty();

            this.leavingCheck();
        }

        // Use function opaque.
        @Override
        public void use() {
            // Switching after cycle requires whole cycle to complete switching process.
            if (this.cycleAwait.isPresent()) {
                try {
                    CyclicBarrier barrier = this.cycleAwait.get();
                    barrier.await();
                    // No we are sure that whole cycle was resolved.
                    this.cycleAwait = Optional.empty();
                } catch (InterruptedException exception) {
                    throw new RuntimeException("panic: unexpected thread interruption");
                } catch (BrokenBarrierException exception) {
                    throw new RuntimeException("panic: unexpected thread interruption");
                }
            }

            // We switched to this workplace, so previous workplace can safely start working.
            if (this.pastWorkplace.isPresent()) {
                SmartWorkplace modifiedWorkplace = this.pastWorkplace.get();
                this.pastWorkplace = Optional.empty();

                try {
                    this.handler.workshopMutex().acquire();
                    modifiedWorkplace.canWork = true;
                    
                    if (modifiedWorkplace.waitingOnSimpleSwitch) {
                        modifiedWorkplace.simpleSwitchWaiting.release();
                    } else {
                        this.handler.workshopMutex().release();
                    }
                } catch (InterruptedException exception) {
                    throw new RuntimeException("panic: unexpected thread interruption");
                }
            }

            // Someone switched so we must perform checks to start working.
            if (!this.simpleSwitchChecked) {
                this.simpleSwitchChecked = true;

                try {
                    this.handler.workshopMutex().acquire();

                    if (!this.canWork) {
                        this.waitingOnSimpleSwitch = true;
                        this.handler.workshopMutex().release();
                        this.simpleSwitchWaiting.acquire();
                        this.waitingOnSimpleSwitch = false;
                    }

                    this.canWork = true;
                    this.handler.workshopMutex().release();
                } catch (InterruptedException e) {
                    throw new RuntimeException("panic: unexpected thread interruption");  
                }
            }

            this.workplace.use();
        }
    }

    // Workshop's workplaces.
    private final Map<WorkplaceId, SmartWorkplace> workplaces;
    private Map<Long, SmartWorkplace> userPlace;

    // Main workshop's mutex.
    private Semaphore mainMutex;
    // Number of places in workshop.
    public final int workplaceCount;
    
    // Simple graph - every node can have at most one outgoing edge.
    // Used to detect cycles in switching process.
    private SimpleGraph workplaceGraph;
    // Queue of events to enforce workshop's liveliness.
    private ActionQueue livelinessQueue;

    public Semaphore workshopMutex() {
        return this.mainMutex;
    }    

    public SmartWorkshop(Collection<Workplace> workplaces) {
        this.workplaceCount = workplaces.size();
        this.mainMutex = new Semaphore(1, true);
        this.workplaceGraph = new SimpleGraph(this.workplaceCount);
        this.livelinessQueue = new ActionQueue(this);
        this.userPlace = new HashMap<>();

        Map<WorkplaceId, SmartWorkplace> workplacesMap = new TreeMap<>();
        int id = 0;
        for (Workplace workplace : workplaces) {
            workplacesMap.put(workplace.getId(), new SmartWorkplace(workplace, this, id));
            id++;
        }

        this.workplaces = workplacesMap;
    }

    // Assigns user of id userId to workplace target.
    protected void reportSwitch(Long userId, SmartWorkplace target) {
        this.userPlace.put(userId, target);
    }

    // Deletes all information about user of id userId.
    protected void reportLeave(Long userId) {
        this.userPlace.remove(userId);
    }

    // Getter to graph.
    protected SimpleGraph workplaceGraph() {
        return this.workplaceGraph;
    }

    // Getter to liveliness queue.
    protected ActionQueue livelinessQueue() {
        return this.livelinessQueue;
    }

    @Override
    public Workplace enter(WorkplaceId wid) {
        try {
            ActionReference reference = this.livelinessQueue.waitForEnter();

            SmartWorkplace workplace = this.workplaces.get(wid);
            workplace.enter();

            reference.markCompleted();

            Long userId = Thread.currentThread().getId();
            this.userPlace.put(userId, workplace);
            
            return workplace;
        } catch (InterruptedException exception) {
            throw new RuntimeException("panic: unexpected thread interruption");
        } finally {
            if (!this.livelinessQueue.tryConsumeMainMutex()) {
                this.mainMutex.release();
            }
        }
    }

    @Override
    public Workplace switchTo(WorkplaceId wid) {
        try {
            this.mainMutex.acquire();
            Long userId = Thread.currentThread().getId();

            SmartWorkplace origin = this.userPlace.get(userId);
            SmartWorkplace target = this.workplaces.get(wid);

            if (origin.getId() == target.getId()) {
                // System.out.println("Target is equal to origin");
                this.mainMutex.release();
                return origin;
            }

            origin.switchTo(target, userId);
            return target;
        } catch (InterruptedException exception) {
            throw new RuntimeException("panic: unexpected thread interruption");
        }
    }

    @Override
    public void leave() {
        try {
            this.mainMutex.acquire();
            Long userId = Thread.currentThread().getId();

            SmartWorkplace userWorkplace = userPlace.get(userId);
            userWorkplace.leave(userId);
            if (TESTING_MODE) System.out.println("Leaving done.");
        } catch (InterruptedException exception) {
            throw new RuntimeException("panic: unexpected thread interruption");
        }
    }
}