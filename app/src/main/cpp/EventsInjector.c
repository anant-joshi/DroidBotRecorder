#include "EventsInjector.h"


/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    intEnableDebug
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_intEnableDebug
        (JNIEnv *env, jclass this_, jint enable) {
    return 0;
}

/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    scanDevices
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_scanDevices
        (JNIEnv *env, jclass this_) {
    return 0;
}

/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    openDevice
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_openDevice
        (JNIEnv *env, jclass this_, jint index) {
    return 0;
}

/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    closeDevice
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_closeDevice
        (JNIEnv *env, jclass this_, jint index) {
    return 0;
}

/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    getDevicePath
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_getDevicePath
        (JNIEnv *env, jclass this_, jint index) {
    return (*env)->NewStringUTF(env, 0);
}

/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    getDeviceName
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_getDeviceName
        (JNIEnv *env, jclass this_, jint index) {
    return (*env)->NewStringUTF(env, 0);
}

/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    pollDevice
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_pollDevice
        (JNIEnv *env, jclass this_, jint index) {
    return 0;
}

/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    getType
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_getType
        (JNIEnv *env, jclass this_) {
    return 0;
}

/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    getCode
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_getCode
        (JNIEnv *env, jclass this_) {
    return 0;
}

/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    getValue
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_getValue
        (JNIEnv *env, jclass this_) {
    return 0;
}

/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    injectEvent
 * Signature: (IIII)I
 */
JNIEXPORT jint JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_injectEvent
        (JNIEnv *env, jclass this_, jint deviceId, jint type, jint code, jint value) {
    return 0;
}

