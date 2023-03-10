package cp2022.solution;

// Reference to event from action queue.
// It allows user to mark this event as completed.
public interface ActionReference {
    void markCompleted();
}
