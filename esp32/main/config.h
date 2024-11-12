#ifndef CONFIG_H
#define CONFIG_H

#include "freertos/FreeRTOS.h"
#include "freertos/task.h"

#define WIFI_SSID      "Your Wifi SSID"
#define WIFI_PASS      "Your Wifi PW"
#define MAXIMUM_RETRY  5

#define STATIC_IP_ADDR "192.168.0.2" //Which IP should the ESP32 always get
#define STATIC_NETMASK "255.255.255.0"
#define STATIC_GATEWAY "192.168.0.1"

#define WEB_SERVER_PORT 8080

#define WIFI_CONNECTED_BIT BIT0
#define WIFI_FAIL_BIT      BIT1

#define MIN(a, b) ((a) < (b) ? (a) : (b))

#endif // CONFIG_H