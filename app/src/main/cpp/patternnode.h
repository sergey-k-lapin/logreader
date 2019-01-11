/*
 * File:   cpatternnode.h
 * Author: sergeyklapin
 *
 * Created on 26 декабря 2018 г., 18:05
 */
#include <cstdlib>

#ifndef CPATTERNNODE_H
#define CPATTERNNODE_H

class PatternNode {
public:
    char type;
    size_t size;
    size_t offset;

    PatternNode(size_t offset, size_t size, char type);
    ~PatternNode();

    PatternNode* prev;
    PatternNode* next;

};

#endif /* CPATTERNNODE_H */

