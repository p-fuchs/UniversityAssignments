package cp2022.solution;

import java.util.concurrent.Semaphore;
import java.util.LinkedList;
import java.util.Queue;

// Queue to enforce liveliness.
public class ActionQueue {
    // Status of user's action.
    private static enum ActionStatus {
        ENTER_STARTED,
        ENTER_COMPLETE,
        SWITCH_STARTED,
        SWITCH_COMPLETE,
        GUARD;

        // Returns completed version of current status.
        public ActionStatus completed() {
            switch (this) {
                case ENTER_STARTED:
                    return ENTER_COMPLETE;

                case SWITCH_STARTED:
                    return SWITCH_COMPLETE;

                default:
                    return this;
            }
        }

        // Checks if this status represent completed status.
        public boolean isCompleted() {
            return (this == this.completed());
        }
    }

    // Reference to event from the queue.
    private static class QueueAction implements ActionReference {
        private final ActionQueue handler;
        private ActionStatus state;

        public QueueAction(ActionStatus state, ActionQueue handler) {
            this.state = state;
            this.handler = handler;
        }

        // Marks this event as completed.
        public void markCompleted() {
            this.state = this.state.completed();

            this.handler.signalUpdate();
        }

        // Getter to this event's state.
        public ActionStatus getState() {
            return this.state;
        }
    }

    // Entered count not including first member in queue
    private int enteredCount;
    private final SmartWorkshop workshop;

    // Queue of actions.
    private Queue<QueueAction> events;

    // How many people awaits to enter the workshop.
    private int waitingEnter;
    // Semaphore to wait for enter permission.
    private Semaphore waitingEnterQueue;

    public ActionQueue(SmartWorkshop workshop) {
        this.enteredCount = 0;
        this.events = new LinkedList<>();
        this.workshop = workshop;

        this.waitingEnter = 0;
        this.waitingEnterQueue = new Semaphore(0, true);
    }

    // Signals that status of queue was somehow updated and
    // queue cleaning could be available.
    protected void signalUpdate() {
        QueueAction event = this.events.peek();
        ActionStatus eventState = event.getState();

        if (!eventState.isCompleted()) {
            return;
        }

        this.events.remove();
        while (!this.events.isEmpty() && this.events.peek().getState().isCompleted()) {
            event = this.events.remove();
            eventState = event.getState();

            if (eventState == ActionStatus.ENTER_COMPLETE) {
                this.enteredCount--;
            }
        }

        if (!this.events.isEmpty()) {
            if (this.events.peek().getState() == ActionStatus.ENTER_STARTED) {
                this.enteredCount--;
            }
        }
    }

    // Inserts not completed enter status at the end of the queue.
    private ActionReference insertEnter() {
        QueueAction enterEvent = new QueueAction(ActionStatus.ENTER_STARTED, this);

        if (!this.events.isEmpty()) {
            this.enteredCount++;
        }

        this.events.add(enterEvent);
        
        return enterEvent;
    }

    // Inserts not switched status at the end of the queue.
    private ActionReference insertSwitch() {
        QueueAction enterEvent = new QueueAction(ActionStatus.SWITCH_STARTED, this);

        this.events.add(enterEvent);
        
        return enterEvent;
    }

    // Function to signal request to enter the workshop.
    // Blocks process if enter cannot be performed because of liveliness.
    public ActionReference waitForEnter() throws InterruptedException {
        this.workshop.workshopMutex().acquire();

        if (this.enteredCount >= 2*this.workshop.workplaceCount - 1) {
            this.waitingEnter++;
            this.workshop.workshopMutex().release();
            this.waitingEnterQueue.acquire();
            this.waitingEnter--;
        }

        return this.insertEnter();
    }

    // Try consume main workshop's mutex in order to give it to some process
    // from waiting to enter queue. If it CONSUMES mutex, it returns true.
    // Otherwise it returns false.
    public boolean tryConsumeMainMutex() {
        if (this.waitingEnter > 0 && this.enteredCount < 2*this.workshop.workplaceCount - 1) {
            if (SmartWorkshop.TESTING_MODE) System.out.println("Consuming mutex for actionQueue.");
            this.waitingEnterQueue.release();
            return true;
        } else {
            return false;
        }
    }

    // Reports that user started switching process.
    public ActionReference reportSwitch() {
        return this.insertSwitch();
    }
}
