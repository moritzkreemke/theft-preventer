#include "http_client.h"
#include "esp_http_client.h"
#include "esp_log.h"
#include <string.h>
#include "device_info.h"

static const char *TAG = "http_client";

void send_post_request(const char* state, const char* lux, const char* temp, const char* id) {
    esp_http_client_config_t config = {
        .url = BACKEND_SERVER_IP "/api/add/event",
        .method = HTTP_METHOD_POST,
    };
    esp_http_client_handle_t client = esp_http_client_init(&config);

    esp_http_client_set_header(client, "X-API-Key", BACKEND_API_KEY);
    esp_http_client_set_header(client, "Content-Type", "application/json");

    bool device_reachable = is_device_reachable();
    const char* connection_status = device_reachable ? "connected" : "disconnected";

    char post_data[256];
    snprintf(post_data, sizeof(post_data), 
             "{\"state\":\"%s\",\"lux\":%s,\"temp\":%s,\"phone\":\"%s\"}", 
             state, lux, temp, connection_status);

    esp_http_client_set_post_field(client, post_data, strlen(post_data));

    // Perform the POST request
    esp_err_t err = esp_http_client_perform(client);
    if (err == ESP_OK) {
        ESP_LOGI(TAG, "POST request sent successfully");
    } else {
        ESP_LOGE(TAG, "POST request failed: %s", esp_err_to_name(err));
    }

    esp_http_client_cleanup(client);
}