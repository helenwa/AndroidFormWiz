#include <jni.h>
#include <string>

extern "C"
jstring
Java_com_wallace_happy_androidformwiz_HomeActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from Helen";
    return env->NewStringUTF(hello.c_str());
}
