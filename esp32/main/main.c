#include <stdio.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_system.h"
#include "esp_log.h"
#include "nvs_flash.h"
#include "wifi_manager.h"
#include "webserver.h"
#include "device_info.h"


static const char *TAG = "main";


void app_main(void)
{
    init_nvs();
    ESP_LOGI(TAG, "ESP_WIFI_MODE_STA");
    
    if (wifi_init_sta()) {
        ESP_LOGI(TAG, "WiFi connected successfully");
        httpd_handle_t server = start_webserver();
        if (server) {
            ESP_LOGI(TAG, "Web server started successfully");
        } else {
            ESP_LOGE(TAG, "Failed to start web server");
        }
    } else {
        ESP_LOGE(TAG, "Failed to connect to WiFi");
    }
}