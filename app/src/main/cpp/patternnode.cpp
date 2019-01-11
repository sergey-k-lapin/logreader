/*
 * File:   cpatternnode.cpp
 * Author: sergeyklapin
 *
 * Created on 26 декабря 2018 г., 18:05
 */

#include "patternnode.h"

PatternNode::PatternNode(size_t offset, size_t size, char type) {
    prev = next = 0;
    this->offset = offset;
    this->size = size;
    this->type = type;
};

PatternNode::~PatternNode() {

};
