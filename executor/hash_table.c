#include "hash_table.h"
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <stdlib.h>

#define MINIMAL_SIZE 64

// Fowler-Noll-Vo hash function definiton based on size_t size.
#if (SIZE_MAX == 0xFFFFFFFFFFFFFFFF)
// size_t has 64 bits
#define FNV_BASE ((size_t)0xcbf29ce484222325)
#define FNV_PRIME ((size_t)0x00000100000001B3)
#define SIZE_T_BYTES ((size_t)8)
#else
// size_t has 32 bits
#define FNV_BASE ((size_t)0x811c9dc5)
#define FNV_PRIME ((size_t)0x01000193)
#define SIZE_T_BYTES ((size_t)4)
#endif

static size_t fnv_hash(pid_t process_id)
{
    size_t hash = FNV_BASE;
    size_t casted_id = (size_t)process_id;

    unsigned char* casted_id_bytes = ((unsigned char*)&casted_id);

    for (size_t byte = 0; byte < SIZE_T_BYTES; byte++) {
        hash = hash ^ ((size_t)casted_id_bytes[byte]);
        hash = hash * FNV_PRIME;
    }

    return hash;
}

// We define load factor as 3/4

static void insert_node(hash_table* ht, list_node* node)
{
    size_t hash = fnv_hash(node->key);
    size_t index = hash % ht->size;

    node->next = ht->nodes[index];
    ht->nodes[index] = node;
}

static void check_table(hash_table* ht)
{
    list_node** old_array = NULL;
    size_t old_size = 0;

    bool rehashing_needed = false;

    if (4 * ht->elements > 3 * ht->size) {
        // We need to increase size and rehash table.

        list_node** new_array = calloc(2 * ht->size, sizeof(list_node*));
        old_array = ht->nodes;
        old_size = ht->size;
        ht->nodes = new_array;

        ht->size *= 2;

        rehashing_needed = true;
    } else if (ht->size != MINIMAL_SIZE && 16 * ht->elements <= 3 * ht->size) {
        // We need to decrease size

        list_node** new_array = calloc(ht->size / 2, sizeof(list_node*));
        old_array = ht->nodes;
        old_size = ht->size;
        ht->nodes = new_array;

        ht->size /= 2;

        rehashing_needed = true;
    }

    if (rehashing_needed) {
        for (size_t ind = 0; ind < old_size; ind++) {
            // We need to rehash all elements.

            list_node* old_element = old_array[ind];

            while (old_element != NULL) {
                list_node* next_element = old_element->next;

                insert_node(ht, old_element);

                old_element = next_element;
            }
        }
    }

    free(old_array);
}

void ht_init(hash_table* ht)
{
    ht->elements = 0;
    ht->size = MINIMAL_SIZE;
    ht->nodes = calloc(MINIMAL_SIZE, sizeof(struct list_node*));
}

void ht_insert(hash_table* ht, pid_t key, size_t value)
{
    list_node* node = malloc(sizeof(struct list_node));

    node->key = key;
    node->value = value;

    insert_node(ht, node);

    ht->elements++;

    check_table(ht);
}

size_t ht_remove(hash_table* ht, pid_t key)
{
    size_t hash = fnv_hash(key);
    size_t index = hash % ht->size;

    list_node** prev_next = (ht->nodes + index);
    list_node* element = ht->nodes[index];

    while (element != NULL && element->key != key) {
        prev_next = &element->next;
        element = element->next;
    }

    if (element == NULL) {
        return 0;
    }

    *prev_next = element->next;
    size_t value = element->value;

    free(element);
    ht->elements--;
    check_table(ht);

    return value;
}

void ht_drop(hash_table* ht)
{
    for (size_t ind = 0; ind < ht->size; ind++) {
        list_node* element = ht->nodes[ind];

        while (element != NULL) {
            list_node* next = element->next;
            free(element);
            element = next;
        }
    }

    free(ht->nodes);
}

size_t ht_size(const hash_table* ht)
{
    return ht->elements;
}