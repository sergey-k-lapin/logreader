/* 
 * File:   pattern.h
 * Author: sergeyklapin
 *
 * Created on 26 декабря 2018 г., 18:16
 */

#include "patternnode.h"

#ifndef PATTERN_H
#define PATTERN_H

class Pattern {
public:
    Pattern(char* str, size_t size);
    void Add(PatternNode* node);
    ~Pattern();
    
    size_t size;
    PatternNode* first;
    PatternNode* last;
    char* src;
    size_t src_size;
    size_t payload;
};

#endif /* PATTERN_H */

