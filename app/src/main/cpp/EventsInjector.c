#include "EventsInjector.h"
#include <string.h>
#include <stdint.h>
#include <jni.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdio.h>
#include <dirent.h>
#include <time.h>
#include <errno.h>

#include <sys/ioctl.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <sys/inotify.h>
#include <sys/limits.h>
#include <sys/poll.h>

#include <linux/input.h>
#include <android/log.h>

int enable_debug = 0;

#define TAG "EventInjector::JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , TAG, __VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)


void debug(char *format, ...) {
    if (enable_debug == 0) return;
    //if (strlen(szDbgfile) == 0) return;

    char message_buffer[4096]; //in this buffer we form the message
    const size_t NUMCHARS = sizeof(message_buffer) / sizeof(message_buffer[0]);
    const size_t LASTCHAR = NUMCHARS - 1;
    //format the input string
    va_list pArgs;
    va_start(pArgs, format);
    // use a bounded buffer size to prevent buffer overruns.  Limit count to
    // character size minus one to allow for a NULL terminating character.
    vsnprintf(message_buffer, NUMCHARS - 1, format, pArgs);
    va_end(pArgs);
    //ensure that the formatted string is NULL-terminated
    message_buffer[LASTCHAR] = '\0';
    char *buff = message_buffer;
    LOGD("%s", buff);
    //TextCallback(szBuffer);
}

/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    intEnableDebug
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_intEnableDebug
        (JNIEnv *env, jclass this_, jint enable) {
    enable_debug = enable;
    return enable_debug;
}


static struct device_types {
    struct pollfd file_descriptors;
    char *device_path;
    char *device_name;
} *pDevs = NULL;
struct pollfd *file_descriptors;
static nfds_t device_count;

const char *device_path = "/dev/input";


struct input_event event;


/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    scanDevices
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_scanDevices
        (JNIEnv *env, jclass this_) {
    device_count = 0;
    char device_name[PATH_MAX];
    char *filename;
    DIR *dir;
    struct dirent *dirent;
    dir = opendir(device_path);

    if (dir == NULL)
        return -1;
    strcpy(device_name, device_path);
    filename = device_name + strlen(device_name);
    *filename++ = '/';
    while ((dirent = readdir(dir))) {
        if (dirent->d_name[0] == '.' &&
            (dirent->d_name[1] == '\0' ||
             (dirent->d_name[1] == '.' && dirent->d_name[2] == '\0')))
            continue;
        strcpy(filename, dirent->d_name);
        debug("scan_dir:prepare to open:%s", device_name);

        // add new filename to our structure: device_name
        struct device_types *new_pDevs = realloc(pDevs, sizeof(pDevs[0]) * (device_count + 1));
        if (new_pDevs == NULL) {
            debug("out of memory");
            return -1;
        }
        pDevs = new_pDevs;

        struct pollfd *new_file_descriptors = realloc(file_descriptors,
                                                      sizeof(file_descriptors[0]) *
                                                      (device_count + 1));
        if (new_file_descriptors == NULL) {
            debug("out of memory");
            return -1;
        }
        file_descriptors = new_file_descriptors;
        file_descriptors[device_count].events = POLLIN;

        pDevs[device_count].file_descriptors.events = POLLIN;
        pDevs[device_count].device_path = strdup(device_name);
        device_count++;
    }
    closedir(dir);
    return device_count;
}

/*
* Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    openDevice
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_openDevice
        (JNIEnv *env, jclass this_, jint index) {
    if (index > device_count || pDevs == NULL) {
        return -1;
    }

    debug("open device: Prepare to open");
    char *device = pDevs[index].device_path;

    debug("open device: opening device %s", device);
    int file_descriptor;
    char name[80];

    file_descriptor = open(device, O_RDWR);
    if (file_descriptor < 0) {
        pDevs[index].file_descriptors.fd = -1;
        pDevs[index].device_name = NULL;
        debug("open device: could not open device '%s', error: %s", device, strerror(errno));
        return -1;
    }

    pDevs[index].file_descriptors.fd = file_descriptor;
    file_descriptors[index].fd = file_descriptor;

    name[sizeof(name) - 1] = '\0';

    if (ioctl(file_descriptor, EVIOCGNAME(sizeof(name) - 1), &name) < 1) {
        debug("open device: could not get device name for device: '%s', error: '%s'", device,
              strerror(errno));
        name[0] = '\0';
    }

    debug("open device: Device: %d %s %s", device_count, device, name);
    pDevs[index].device_name = strdup(name);
    return 0;
}



/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    closeDevice
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_closeDevice
        (JNIEnv *env, jclass this_, jint index) {
    if (index >= device_count || pDevs == NULL) {
        return -1;
    }

    int count = device_count - index - 1;
    debug("remove device: removing device %d", index);
    free(pDevs[index].device_name);
    free(pDevs[index].device_path);

    memmove(&pDevs[index], &pDevs[index + 1], sizeof(pDevs[0]) * count);
    device_count--;
    return 0;
}


/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    getDevicePath
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_getDevicePath
        (JNIEnv *env, jclass this_, jint index) {
    return (*env)->NewStringUTF(env, pDevs[index].device_path);
}

/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    getDeviceName
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_getDeviceName
        (JNIEnv *env, jclass this_, jint index) {
    return (*env)->NewStringUTF(env, pDevs[index].device_name);
}

/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    pollDevice
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_pollDevice
        (JNIEnv *env, jclass this_, jint index) {
    if (index >= device_count || pDevs[index].file_descriptors.fd == -1) return -1;
    poll(file_descriptors, device_count, -1);
    if (file_descriptors[index].revents) {
        if (file_descriptors[index].revents & POLLIN) {
            int res = read(file_descriptors[index].fd, &event, sizeof(event));
            if (res < (int) sizeof(event)) {
                return 1;
            } else return 0;
        }
    }
    return -1;
}

/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    getType
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_getType
        (JNIEnv *env, jclass this_) {
    return event.type;
}

/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    getCode
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_getCode
        (JNIEnv *env, jclass this_) {
    return event.code;
}

/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    getValue
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_getValue
        (JNIEnv *env, jclass this_) {
    return event.value;
}

/*
 * Class:     org_honeynet_droidbotrecorder_injection_EventsInjector
 * Method:    injectEvent
 * Signature: (IIII)I
 */
JNIEXPORT jint JNICALL Java_org_honeynet_droidbotrecorder_injection_EventsInjector_injectEvent
        (JNIEnv *env, jclass this_, jint deviceId, jint type, jint code, jint value) {
    int index = deviceId;
    if (index >= device_count || pDevs[index].file_descriptors.fd == -1) return -1;
    int fd = pDevs[index].file_descriptors.fd;
    debug("SendEvent call (%d,%d,%d,%d)", fd, type, code, value);
    struct uinput_event event;
    ssize_t len;
    if (fd <= fileno(stderr)) return -1;

    memset(&event, 0, sizeof(event));
    event.type = (uint16_t) type;
    event.code = (uint16_t) code;
    event.value = (uint16_t) value;
    len = write(fd, &event, sizeof(event));
    debug("SendEvent done:%d", len);
    return 0;
}

