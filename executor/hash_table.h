#ifndef HASH_TABLE_H_
#define HASH_TABLE_H_

#include <unistd.h>

typedef struct list_node {
    pid_t key;
    size_t value;

    struct list_node* next;
} list_node;

struct hash_table {
    list_node** nodes;
    size_t size;
    size_t elements;
};

typedef struct hash_table hash_table;

void ht_init(hash_table* ht);

void ht_insert(hash_table* ht, pid_t key, size_t value);

size_t ht_remove(hash_table* ht, pid_t key);

void ht_drop(hash_table* ht);

size_t ht_size(const hash_table* ht);

#endif