#include "device_info.h"
#include "nvs_flash.h"
#include "nvs.h"
#include "esp_log.h"
#include <string.h>
#include "ping/ping_sock.h"
#include <stdbool.h>
#include "lwip/inet.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "lwip/ip4_addr.h"
#include "lwip/ip_addr.h"
#include "lwip/sockets.h" 


#define PING_COUNT 3
#define PING_TIMEOUT_MS 1000
#define PING_DELAY_MS 1000

static const char *TAG = "device_info";

device_info_t registered_device = {0};

esp_err_t load_device_info(void);

esp_err_t init_nvs(void)
{
    esp_err_t err = nvs_flash_init();
    if (err == ESP_ERR_NVS_NO_FREE_PAGES || err == ESP_ERR_NVS_NEW_VERSION_FOUND) {
        ESP_ERROR_CHECK(nvs_flash_erase());
        err = nvs_flash_init();
    }
    err = load_device_info();
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to load device info: %s", esp_err_to_name(err));
        err = ESP_OK;
    } else {
        ESP_LOGI(TAG, "Device info loaded successfully");
        ESP_LOGI(TAG, "Loaded IP: %s", registered_device.ip);
    }
    return err;
}

esp_err_t save_device_info(const char* ip)
{
    nvs_handle_t my_handle;
    esp_err_t err;

    // Open NVS handle
    err = nvs_open("storage", NVS_READWRITE, &my_handle);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Error (%s) opening NVS handle!", esp_err_to_name(err));
        return err;
    }

    // Copy IP to registered_device structure
    strncpy(registered_device.ip, ip, sizeof(registered_device.ip) - 1);
    registered_device.ip[sizeof(registered_device.ip) - 1] = '\0';

    err = nvs_set_str(my_handle, "device_ip", ip);
    if (err != ESP_OK) {
        nvs_close(my_handle);
        return err;
    }

    err = nvs_commit(my_handle);
    if (err != ESP_OK) {
        nvs_close(my_handle);
        return err;
    }

    nvs_close(my_handle);
    return ESP_OK;
}

esp_err_t load_device_info()
{
    nvs_handle_t my_handle;
    esp_err_t err;

    // Open NVS handle
    err = nvs_open("storage", NVS_READONLY, &my_handle);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Error (%s) opening NVS handle!", esp_err_to_name(err));
        return err;
    }

    size_t ip_len = sizeof(registered_device.ip);

    err = nvs_get_str(my_handle, "device_ip", registered_device.ip, &ip_len);
    if (err == ESP_ERR_NVS_NOT_FOUND) {
        ESP_LOGI(TAG, "Device IP not found in NVS");
        registered_device.ip[0] = '\0';  // Set to empty string if not found
        nvs_close(my_handle);
        return ESP_OK;  // Not an error, just not set yet
    } else if (err != ESP_OK) {
        ESP_LOGE(TAG, "Error reading device IP from NVS: %s", esp_err_to_name(err));
        nvs_close(my_handle);
        return err;
    }

    registered_device.ip[sizeof(registered_device.ip) - 1] = '\0';  // Ensure null-termination
    ESP_LOGI(TAG, "Loaded device IP: %s", registered_device.ip);

    nvs_close(my_handle);
    return ESP_OK;
}

esp_err_t get_device_info(device_info_t* device_info)
{
    if (device_info == NULL) {
        return ESP_ERR_INVALID_ARG;
    }
    memcpy(device_info, &registered_device, sizeof(device_info_t));
    return ESP_OK;
}

bool is_device_reachable()
{
    if (registered_device.ip[0] == '\0') {
        ESP_LOGE(TAG, "Invalid IP address");
        return false;
    }

    ip4_addr_t target_addr;
    if (ip4addr_aton(registered_device.ip, &target_addr) == 0) {
        ESP_LOGE(TAG, "Invalid IP address format");
        return false;
    }

    esp_ping_config_t ping_config = ESP_PING_DEFAULT_CONFIG();
    ping_config.target_addr.u_addr.ip4 = target_addr;
    ping_config.target_addr.type = IPADDR_TYPE_V4;
    ping_config.count = PING_COUNT;
    ping_config.timeout_ms = PING_TIMEOUT_MS;
    ping_config.interval_ms = PING_DELAY_MS;

    esp_ping_handle_t ping_handle;
    esp_err_t err = esp_ping_new_session(&ping_config, NULL, &ping_handle);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Error creating ping session: %s", esp_err_to_name(err));
        return false;
    }

    err = esp_ping_start(ping_handle);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Error starting ping: %s", esp_err_to_name(err));
        esp_ping_delete_session(ping_handle);
        return false;
    }

    // Wait for ping to complete
    vTaskDelay(pdMS_TO_TICKS(PING_COUNT * PING_DELAY_MS + 1000));

    // Check if any responses were received
    uint32_t received = 0;
    esp_ping_get_profile(ping_handle, ESP_PING_PROF_REPLY, &received, sizeof(received));

    esp_ping_delete_session(ping_handle);

    if (received > 0) {
        ESP_LOGI(TAG, "Device is reachable");
        return true;
    } else {
        ESP_LOGI(TAG, "Device is not reachable");
        return false;
    }
}