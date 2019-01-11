/*
 * File:   clogreader.cpp
 * Author: sergeyklapin
 *
 * Created on 26 декабря 2018 г., 18:47
 */

#include "clogreader.h"

CLogReader::CLogReader() {
    this->pattern = 0;
}

CLogReader::~CLogReader() {
    if (pattern){
    delete pattern;
    }
}

size_t CLogReader::strlen(const char* str) {
    size_t count = 0;
    do {
        ++count;
    } while (*(str + count));
    return count;
}

bool CLogReader::SetFilter(const char* filter) {
    return SetFilter(filter, strlen(filter));
}

bool CLogReader::SetFilter(const char* filter, const size_t filter_size) {
    if (pattern) {
        delete pattern;
    }
    pattern = new Pattern((char*) filter, filter_size);
    if (pattern->payload == 0){
        delete pattern;
        return false;
    }
    return true;
}

bool CLogReader::AddSourceBlock(const char* block, const size_t block_size) {
    if (pattern && pattern->payload) {
        return search(block, block_size);
    }
    return false;
}

bool CLogReader::search(const char *block, const size_t block_size){
    char* str = (char*)block;
    unsigned int str_size = block_size;
    int offset = 0;
    int seek;
    int seek_pos;
    for (PatternNode* n = pattern->first; n; n = n->next) {
        if (n->type == 'T') {
            seek_pos = seek_substring_KMP(str + offset, str_size - offset, pattern->src + n->offset, n->size);
            if (seek_pos != -1) {
                seek = offset+seek_pos;
                if (

                        !(n->prev && n->prev->type == 'S' && seek > 0) //Если есть вперени  последовательности и выражение найдено не в начале строки
                        &&
                        !(n->prev && n->prev->type == 'C' && seek == offset + n->prev->size) //Если есть вперени  последовательности и выражение найдено не в начале строки
                        &&
                        !(!n->prev && seek == 0) // Это первый элемент паттерна и выражение найдено в начале строки

                        ) {
                    return false;
                }
                if (
                        !(n->next && n->next->type == 'S' && seek + n->size + n->next->size < str_size) //Есть далее послеовательность и это не последнее выражение с учетом отступа
                        &&
                        !(n->next && n->next->type == 'C' && seek + n->size + n->next->size <= str_size) //Есть далее послеовательность и это не последнее выражение с учетом отступа
                        &&
                        !(!n->next && seek + n->size == str_size) //Последний элемент паттерна и это конец строки с учетом отсупа
                        ) {
                    return false;
                }
                offset = seek + n->size;
            } else {
                return false;
            }
        }
    }
    return true;
};

int CLogReader::seek_substring_KMP(char* s, int size_s, char* p, int size_p) {
    int i, j, N, M;
    N = size_s;
    M = size_p;

    // Динамический массив длины М
    int *d = (int*) malloc(M * sizeof (int));

    // Вычисление префикс-функции
    d[0] = 0;
    for (i = 1, j = 0; i < M; i++) {
        while (j > 0 && p[j] != p[i])
            j = d[j - 1];
        if (p[j] == p[i])
            j++;
        d[i] = j;
    }

    // Поиск
    for (i = 0, j = 0; i < N; i++) {
        while (j > 0 && p[j] != s[i])
            j = d[j - 1];
        if (p[j] == s[i])
            j++;
        if (j == M) {
            free(d);
            return i - j + 1;
        }
    }
    free(d);
    return -1;
}