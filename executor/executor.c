#include <errno.h>
#include <fcntl.h>
#include <pthread.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/select.h>
#include <sys/wait.h>
#include <unistd.h>

#include "err.h"
#include "execute_utils.h"
#include "hash_table.h"
#include "utils.h"

typedef struct {
    pthread_mutex_t out_mutex;
    char out_buffer[MAX_COM_LEN + 1];
    pthread_t stdout_thread;

    pthread_mutex_t err_mutex;
    char err_buffer[MAX_COM_LEN + 1];
    pthread_t stderr_thread;

    pid_t process_id;
    bool started;
} executor_task;

typedef struct {
    int out_pipe;
    int err_pipe;
    size_t task_id;
    bool watching_out;
} run_args;

executor_task tasks[MAX_N_TASKS];

run_args* clone_thread_args(const run_args* args)
{
    run_args* thread_args = malloc(sizeof(run_args));

    thread_args->out_pipe = args->out_pipe;
    thread_args->err_pipe = args->err_pipe;
    thread_args->task_id = args->task_id;
    thread_args->watching_out = args->watching_out;

    return thread_args;
}

void thread_task(int descriptor, pthread_mutex_t* buffer_mutex, char* outer_buffer)
{
    FILE* stream = fdopen(descriptor, "r");
    if (stream == NULL) {
        fprintf(stderr, "Unable to convert descriptor to stream.");
        exit(1);
    }

    char thread_buffer[MAX_COM_LEN + 1];

    while (fgets(thread_buffer, MAX_COM_LEN + 1, stream)) {
        ASSERT_ZERO(pthread_mutex_lock(buffer_mutex));

        strcpy(outer_buffer, thread_buffer);
        outer_buffer[strcspn(outer_buffer, "\n")] = 0;

        ASSERT_ZERO(pthread_mutex_unlock(buffer_mutex));
    }

    ASSERT_ZERO(fclose(stream));
}

void* run_thread_task(void* arg)
{
    run_args* thread_args = (run_args*)arg;
    int descriptor;
    pthread_mutex_t* buffer_mutex;
    char* outer_buffer;

    if (thread_args->watching_out) {
        descriptor = thread_args->out_pipe;
        buffer_mutex = &tasks[thread_args->task_id].out_mutex;
        outer_buffer = tasks[thread_args->task_id].out_buffer;
    } else {
        descriptor = thread_args->err_pipe;
        buffer_mutex = &tasks[thread_args->task_id].err_mutex;
        outer_buffer = tasks[thread_args->task_id].err_buffer;
    }

    thread_task(descriptor, buffer_mutex, outer_buffer);

    free(arg);
    return NULL;
}

void run_command(char* const* splitted_command, size_t* task_id, pid_t* task_pid)
{
    static size_t running_tasks = 0;

    *task_id = running_tasks++;

    tasks[*task_id].started = true;

    int out_pipe[2];
    int err_pipe[2];

    ASSERT_SYS_OK(pipe(out_pipe));
    ASSERT_SYS_OK(pipe(err_pipe));

    ASSERT_SYS_OK(fcntl(out_pipe[0], F_SETFD, FD_CLOEXEC));
    ASSERT_SYS_OK(fcntl(err_pipe[0], F_SETFD, FD_CLOEXEC));

    pid_t process_id;
    ASSERT_SYS_OK(process_id = fork());

    if (!process_id) {
        // We are the child process.
        // ASSERT_SYS_OK(close(STDIN_FILENO));
        ASSERT_SYS_OK(close(STDOUT_FILENO));
        ASSERT_SYS_OK(close(STDERR_FILENO));

        ASSERT_SYS_OK(dup2(out_pipe[1], STDOUT_FILENO));
        ASSERT_SYS_OK(dup2(err_pipe[1], STDERR_FILENO));

        ASSERT_SYS_OK(close(out_pipe[1]));
        ASSERT_SYS_OK(close(err_pipe[1]));

        ASSERT_SYS_OK(execvp(splitted_command[1], splitted_command + 1));
    } else {
        // We are the process parent.
        *task_pid = process_id;

        ASSERT_SYS_OK(close(out_pipe[1]));
        ASSERT_SYS_OK(close(err_pipe[1]));

        // We spawn a thread to manage reading err and out of process.
        run_args* out_args = malloc(sizeof(run_args));
        out_args->out_pipe = out_pipe[0];
        out_args->err_pipe = err_pipe[0];
        out_args->task_id = *task_id;
        out_args->watching_out = true;

        run_args* err_args = clone_thread_args(out_args);
        err_args->watching_out = false;

        ASSERT_ZERO(pthread_create(&tasks[*task_id].stdout_thread, NULL, run_thread_task, out_args));
        ASSERT_ZERO(pthread_create(&tasks[*task_id].stderr_thread, NULL, run_thread_task, err_args));
    }
}

void out_command(size_t task_id)
{
    pthread_mutex_t* buffer_mutex = &tasks[task_id].out_mutex;

    ASSERT_ZERO(pthread_mutex_lock(buffer_mutex));

    fprintf(stdout, "Task %zu stdout: '%s'.\n", task_id, tasks[task_id].out_buffer);

    ASSERT_ZERO(pthread_mutex_unlock(buffer_mutex));
}

void err_command(size_t task_id)
{
    pthread_mutex_t* buffer_mutex = &tasks[task_id].err_mutex;

    ASSERT_ZERO(pthread_mutex_lock(buffer_mutex));

    fprintf(stdout, "Task %zu stderr: '%s'.\n", task_id, tasks[task_id].err_buffer);

    ASSERT_ZERO(pthread_mutex_unlock(buffer_mutex));
}

void kill_command(size_t task_id)
{
    kill(tasks[task_id].process_id, SIGINT);
}

void quit_command()
{
    // Wait for all threads to finish their jobs.
    // By sending signal to all processes and wait.
    for (size_t index = 0; index < MAX_N_TASKS; index++) {
        if (tasks[index].started) {
            kill(tasks[index].process_id, SIGKILL);
        }
    }

    size_t index = 0;
    while (index < MAX_N_TASKS && tasks[index].started) {
        ASSERT_ZERO(pthread_join(tasks[index].stdout_thread, NULL));
        ASSERT_ZERO(pthread_join(tasks[index].stderr_thread, NULL));

        index++;
    }
}

void init_globals()
{
    for (size_t i = 0; i < MAX_N_TASKS; i++) {
        ASSERT_ZERO(pthread_mutex_init(&tasks[i].err_mutex, NULL));
        ASSERT_ZERO(pthread_mutex_init(&tasks[i].out_mutex, NULL));

        tasks[i].out_buffer[0] = 0;
        tasks[i].err_buffer[0] = 0;
        tasks[i].started = false;
    }
}

void destroy_globals()
{
    for (size_t i = 0; i < MAX_N_TASKS; i++) {
        ASSERT_ZERO(pthread_mutex_destroy(&tasks[i].err_mutex));
        ASSERT_ZERO(pthread_mutex_destroy(&tasks[i].out_mutex));
    }
}

typedef struct {
    pthread_mutex_t writing_lock;

    pthread_cond_t wait_for_process;

    pthread_mutex_t map_lock;

    hash_table pid_task_map;
    bool tasks_ended;
} information_args;

void* process_information_task(void* arg)
{
    information_args* args = arg;

    while (true) {
        // Infinite loop - only breaks at cancellation by args->task_ended with no more tasks to process.
        ASSERT_ZERO(pthread_mutex_lock(&args->map_lock));

        while (ht_size(&args->pid_task_map) == 0) {
            if (args->tasks_ended) {
                // Our work has ended.
                ASSERT_ZERO(pthread_mutex_unlock(&args->map_lock));
                pthread_exit(NULL);
            }

            // We have no process to wait for.
            ASSERT_ZERO(pthread_cond_wait(&args->wait_for_process, &args->map_lock));
        }

        ASSERT_ZERO(pthread_mutex_unlock(&args->map_lock));

        pid_t pid;
        int process_status;

        errno = 0;

        pid = wait(&process_status);
        while (errno == 0) {
            ASSERT_ZERO(pthread_mutex_lock(&args->writing_lock));

            do {
                ASSERT_ZERO(pthread_mutex_lock(&args->map_lock));

                size_t task_id = ht_remove(&args->pid_task_map, pid);

                ASSERT_ZERO(pthread_mutex_unlock(&args->map_lock));

                if (WIFSIGNALED(process_status)) {
                    fprintf(stdout, "Task %zu ended: signalled.\n", task_id);
                } else if (WIFEXITED(process_status)) {
                    fprintf(stdout, "Task %zu ended: status %i.\n", task_id, WEXITSTATUS(process_status));
                } else {
                    fprintf(stderr, "Unknown type of process termination.\n");
                    exit(1);
                }

                pid = waitpid(-1, &process_status, WNOHANG);
            } while (pid > 0);

            ASSERT_ZERO(pthread_mutex_unlock(&args->writing_lock));

            pid = wait(&process_status);
        }

        if (errno != ECHILD) {
            // Some other error than no children to wait for.
            int error = errno;

            fprintf(stdout, "Errno of wait() call is %i\n", error);
            exit(1);
        }
    }
}

void init_config(information_args* args)
{
    pthread_mutexattr_t writing_attr;
    ASSERT_ZERO(pthread_mutexattr_init(&writing_attr));
    ASSERT_ZERO(pthread_mutexattr_setprotocol(&writing_attr, PTHREAD_PRIO_INHERIT));

    ASSERT_ZERO(pthread_mutex_init(&args->writing_lock, &writing_attr));
    ASSERT_ZERO(pthread_mutex_init(&args->map_lock, NULL));

    ht_init(&args->pid_task_map);
    args->tasks_ended = false;

    ASSERT_ZERO(pthread_cond_init(&args->wait_for_process, NULL));

    ASSERT_ZERO(pthread_mutexattr_destroy(&writing_attr));
}

void destroy_config(information_args* args)
{
    ASSERT_ZERO(pthread_mutex_destroy(&args->writing_lock));
    ASSERT_ZERO(pthread_mutex_destroy(&args->map_lock));

    ht_drop(&args->pid_task_map);

    ASSERT_ZERO(pthread_cond_destroy(&args->wait_for_process));
}

int main()
{
    char com_buffer[MAX_COM_LEN + 1];

    information_args helper_args;
    init_config(&helper_args);

    pthread_t helper_handler;

    ASSERT_ZERO(pthread_create(&helper_handler, NULL, process_information_task, &helper_args));

    init_globals();

    while (read_line(com_buffer, MAX_COM_LEN + 1, stdin)) {
        // We must remove new-line char from buffer
        com_buffer[strcspn(com_buffer, "\n")] = '\0';

        char** splitted_command = split_string(com_buffer);

        char* command = splitted_command[0];

        ASSERT_ZERO(pthread_mutex_lock(&helper_args.writing_lock));
        if (strcmp("run", command) == 0) {
            size_t task_id;
            pid_t task_pid = 0;

            run_command(splitted_command, &task_id, &task_pid);
            tasks[task_id].process_id = task_pid;

            ASSERT_ZERO(pthread_mutex_lock(&helper_args.map_lock));
            ht_insert(&helper_args.pid_task_map, task_pid, task_id);

            ASSERT_ZERO(pthread_cond_signal(&helper_args.wait_for_process));
            ASSERT_ZERO(pthread_mutex_unlock(&helper_args.map_lock));

            fprintf(stdout, "Task %zu started: pid %d.\n", task_id, task_pid);
        } else if (strcmp("out", command) == 0) {
            size_t task_id;

            sscanf(com_buffer, "out %zu", &task_id);
            out_command(task_id);
        } else if (strcmp("err", command) == 0) {
            size_t task_id;

            sscanf(com_buffer, "err %zu", &task_id);
            err_command(task_id);
        } else if (strcmp("kill", command) == 0) {
            size_t task_id;

            sscanf(com_buffer, "kill %zu", &task_id);
            kill_command(task_id);
        } else if (strcmp("sleep", command) == 0) {
            size_t mills;

            sscanf(com_buffer, "sleep %zu", &mills);
            usleep(1000 * mills);
        } else if (strcmp("quit", command) == 0) {
            ASSERT_ZERO(pthread_mutex_unlock(&helper_args.writing_lock));
            free_split_string(splitted_command);

            break;
        } else if (strcmp("", command) != 0) {
            fprintf(stderr, "Unexpected command was read: [%s].\n", command);
            exit(1);
        }

        ASSERT_ZERO(pthread_mutex_unlock(&helper_args.writing_lock));

        free_split_string(splitted_command);
    }

    ASSERT_ZERO(pthread_mutex_lock(&helper_args.map_lock));

    helper_args.tasks_ended = true;
    ASSERT_ZERO(pthread_cond_signal(&helper_args.wait_for_process));

    ASSERT_ZERO(pthread_mutex_unlock(&helper_args.map_lock));

    quit_command();

    ASSERT_ZERO(pthread_join(helper_handler, NULL));

    destroy_config(&helper_args);

    destroy_globals();
}