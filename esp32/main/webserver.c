#include "device_info.h"
#include "webserver.h"
#include "esp_http_server.h"
#include "esp_log.h"
#include "http_client.h"
#include "config.h"
#include "esp_wifi.h"
#include <string.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <lwip/inet.h>
#include <lwip/netif.h>
#include <lwip/etharp.h>
#include <lwip/ip4_addr.h>
#include "esp_netif.h" 
#include "esp_netif_net_stack.h"

static const char *TAG = "webserver";


static esp_err_t register_device_handler(httpd_req_t *req)
{
    char ip_str[INET_ADDRSTRLEN] = {0};

    int sockfd = httpd_req_to_sockfd(req);

    struct sockaddr_storage addr;
    socklen_t addr_len = sizeof(addr);
    if (getpeername(sockfd, (struct sockaddr *)&addr, &addr_len) == -1) {
        ESP_LOGE(TAG, "Failed to get peer name");
        const char* resp_str = "Failed to get peer name";
        httpd_resp_send(req, resp_str, strlen(resp_str));
        return ESP_FAIL;
    }

    // Handle IPv4 and IPv4-mapped IPv6 addresses
    ip4_addr_t target_addr;
    if (addr.ss_family == AF_INET6) {
        struct sockaddr_in6 *addr6 = (struct sockaddr_in6 *)&addr;
        if (IN6_IS_ADDR_V4MAPPED(&addr6->sin6_addr)) {
            memcpy(&target_addr.addr, &addr6->sin6_addr.s6_addr[12], sizeof(target_addr.addr));
        } else {
            ESP_LOGE(TAG, "Pure IPv6 address not supported");
            const char* resp_str = "IPv6 not supported";
            httpd_resp_send(req, resp_str, strlen(resp_str));
            return ESP_FAIL;
        }
    } else if (addr.ss_family == AF_INET) {
        struct sockaddr_in *addr_in = (struct sockaddr_in *)&addr;
        target_addr.addr = addr_in->sin_addr.s_addr;
    } else {
        ESP_LOGE(TAG, "Unknown address family");
        const char* resp_str = "Unknown address family";
        httpd_resp_send(req, resp_str, strlen(resp_str));
        return ESP_FAIL;
    }

    inet_ntop(AF_INET, &target_addr.addr, ip_str, INET_ADDRSTRLEN);
    ESP_LOGI(TAG, "Client IP address: %s", ip_str);

    if (save_device_info(ip_str) == ESP_OK) {
        const char* resp_str = "Device registered successfully";
        httpd_resp_send(req, resp_str, strlen(resp_str));
    } else {
        const char* resp_str = "Failed to register device";
        httpd_resp_send(req, resp_str, strlen(resp_str));
    }

    return ESP_OK;
}

static esp_err_t receive_data_handler(httpd_req_t *req)
{
    char response[80];
     snprintf(response, sizeof(response), "IP: %s", registered_device.ip);
    ESP_LOGI(TAG, "Returning registered device data: %s", response);
    httpd_resp_send(req, response, strlen(response));
    return ESP_OK;
}

static esp_err_t root_get_handler(httpd_req_t *req)
{
    char*  buf;
    size_t buf_len;

    buf_len = httpd_req_get_url_query_len(req) + 1;
    if (buf_len > 1) {
        buf = malloc(buf_len);
        if (httpd_req_get_url_query_str(req, buf, buf_len) == ESP_OK) {
            char state[10] = {0}, lux[10] = {0}, temp[10] = {0}, id[50] = {0};
            
            if (httpd_query_key_value(buf, "state", state, sizeof(state)) == ESP_OK &&
                httpd_query_key_value(buf, "lux", lux, sizeof(lux)) == ESP_OK &&
                httpd_query_key_value(buf, "temp", temp, sizeof(temp)) == ESP_OK &&
                httpd_query_key_value(buf, "id", id, sizeof(id)) == ESP_OK) {
                
                ESP_LOGI(TAG, "Received: state=%s, lux=%s, temp=%s, id=%s", state, lux, temp, id);
                
                // Send POST request with received data
                send_post_request(state, lux, temp, id);
                
                const char* resp_str = "Parameters received and POST request sent";
                httpd_resp_send(req, resp_str, strlen(resp_str));
            } else {
                const char* resp_str = "Invalid parameters";
                httpd_resp_send(req, resp_str, strlen(resp_str));
            }
        }
        free(buf);
    } else {
        const char* resp_str = "No parameters found";
        httpd_resp_send(req, resp_str, strlen(resp_str));
    }
    return ESP_OK;
}


// HTTP GET Handler
static esp_err_t hello_get_handler(httpd_req_t *req)
{
    ESP_LOGI(TAG, "Received a GET request for %s", req->uri);
    const char* resp_str = "Hello from ESP32!";
    httpd_resp_send(req, resp_str, strlen(resp_str));
    return ESP_OK;
}

static httpd_uri_t root = {
    .uri       = "/",
    .method    = HTTP_GET,
    .handler   = root_get_handler,
    .user_ctx  = NULL
};

static httpd_uri_t hello = {
    .uri       = "/hello",
    .method    = HTTP_GET,
    .handler   = hello_get_handler,
    .user_ctx  = NULL
};

static httpd_uri_t register_device_uri = {
    .uri       = "/register_device",
    .method    = HTTP_GET,
    .handler   = register_device_handler,
    .user_ctx  = NULL
};

static httpd_uri_t receive_data_uri = {
    .uri       = "/receive_data",
    .method    = HTTP_GET,
    .handler   = receive_data_handler,
    .user_ctx  = NULL
};

httpd_handle_t start_webserver(void)
{
    httpd_handle_t server = NULL;
    httpd_config_t config = HTTPD_DEFAULT_CONFIG();
    config.server_port = WEB_SERVER_PORT;
    ESP_LOGI(TAG, "Starting server on port: '%d'", config.server_port);
    if (httpd_start(&server, &config) == ESP_OK) {
        ESP_LOGI(TAG, "Registering URI handlers");
        httpd_register_uri_handler(server, &root); 
        httpd_register_uri_handler(server, &hello);
        httpd_register_uri_handler(server, &register_device_uri);
        httpd_register_uri_handler(server, &receive_data_uri);
        return server;
    }

    ESP_LOGI(TAG, "Error starting server!");
    return NULL;
}
