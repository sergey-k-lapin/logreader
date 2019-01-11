/*
 * File:   clogreader.h
 * Author: sergeyklapin
 *
 * Created on 26 декабря 2018 г., 18:47
 */

#include <cstdlib>
#include "pattern.h"

#ifndef CLOGREADER_H
#define CLOGREADER_H

class CLogReader {
public:
    CLogReader();
    ~CLogReader();
    bool SetFilter(const char *filter);
    bool SetFilter(const char *filter, const size_t filter_size);
    bool AddSourceBlock(const char *block, const size_t block_size);
protected:
    Pattern* pattern ;
    bool search(const char *block, const size_t block_size);
    int seek_substring_KMP(char* s, int size_s, char* p, int size_p);
    size_t strlen(const char* str);
};

#endif /* CLOGREADER_H */

