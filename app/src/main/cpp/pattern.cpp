/*
 * File:   pattern.cpp
 * Author: sergeyklapin
 *
 * Created on 26 декабря 2018 г., 18:16
 */
#include <cstring>
#include "pattern.h"

Pattern::Pattern(char* src, size_t src_size) {
    this->size = 0;
    this->first = 0;
    this->last = 0;
    this->payload = 0;    

    this->src = (char*)malloc(src_size);
    memcpy(this->src, src, src_size);

    this->src_size = src_size;
    
    size_t offset = 0;
    size_t size = 0;
    for (unsigned int i = 0; i < src_size;) {
        char c = *(src + i);
        if (c == '*') {
            size = i - offset;
            if (size > 0) {
                this->Add(new PatternNode(offset, size, 'T'));
                ++payload;
            }
            if (!last || last->type != 'S') {
                this->Add(new PatternNode(i, 0, 'S'));
            }
            ++i;
            offset = i;
            continue;
        }
        if (c == '?') {
            size = i - offset;
            if (size > 0) {
                this->Add(new PatternNode(offset, size, 'T'));
                ++payload;
            }
            if (!last || last->type != 'C') {
                this->Add(new PatternNode(i, 1, 'C'));
            } else {
                ++last->size;
            }
            ++i;
            offset = i;
            continue;
        }
        if (i == src_size - 1) {
            ++i;
            size = i - offset;
            if (size > 0) {
                this->Add(new PatternNode(offset, size, 'T'));
                ++payload;
            }
            continue;
        }
        ++i;
    }
}

Pattern::~Pattern() {
    PatternNode *n;
    while (first) {
        n=first->next;
        delete first;
        first=n;
    }
    free(this->src);
}

void Pattern::Add(PatternNode* node) {
    if (this->size == 0) {
        first = last = node;
    } else {
        last->next = node;
        node->prev = last;
        last = node;
    }
    ++size;
}
