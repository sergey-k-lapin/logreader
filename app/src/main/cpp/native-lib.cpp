#include <jni.h>
#include <string>
#include "clogreader.h"

class LogReader {
public:
    static CLogReader *getInstance(){
        if (instance == NULL){
            instance = new CLogReader();
        }
        return instance;
    }
private:
    LogReader(){}
    LogReader( const LogReader& );
    LogReader& operator=( LogReader& );

    static CLogReader *instance;
};

CLogReader* LogReader::instance = NULL;

extern "C" JNIEXPORT jstring JNICALL
Java_com_lapingames_logreader_JLogReader_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_lapingames_logreader_JLogReader_SetFilter(
        JNIEnv *env,
        jobject /* this */,
        jstring pattern,
        jint pattern_size) {
    const char *pattern_c = env->GetStringUTFChars(pattern, 0);
    bool result = LogReader::getInstance()->SetFilter(pattern_c, (const size_t)pattern_size);
    env->ReleaseStringUTFChars(pattern, pattern_c);
    return (jboolean)result;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_lapingames_logreader_JLogReader_AddSourceBlock(
        JNIEnv *env,
        jobject /* this */,
        jstring block,
        jint block_size) {
    const char *block_c = env->GetStringUTFChars(block, 0);
    bool result = LogReader::getInstance()->AddSourceBlock(block_c, (const size_t)block_size);
    env->ReleaseStringUTFChars(block, block_c);
    return (jboolean)result;
}
