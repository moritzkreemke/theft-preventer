#ifndef DEVICE_INFO_H
#define DEVICE_INFO_H

#include "esp_err.h"
#include <stdbool.h>

typedef struct {
    char ip[40];
} device_info_t;


extern device_info_t registered_device;

esp_err_t init_nvs(void);
esp_err_t save_device_info(const char* ip);
esp_err_t get_device_info(device_info_t* device_info);
bool is_device_reachable();
#endif // DEVICE_INFO_H