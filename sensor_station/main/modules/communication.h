
#ifndef COMMUNICATION_H
#define COMMUNICATION_H

#include <stdbool.h>

#include "../models/sensor_station_info.h"
#include "../models/sensor_data.h"
#include "../models/sensor_types.h"

#include "../util/uuid.h"

bool initialize_communication(uuid_t);

void enable_pairing_mode();
void disable_pairing_mode();

// Set Event Handler that is called when a device connects/disconnects.
void set_connected_event_handler(void (*handler)());
void set_disconnected_event_handler(void (*handler)());

// Set Event Handler that is called when a Characteristic is changed
void set_data_read_flag_set_event_handler(void (*handler)());
void clear_data_read_flag();

void set_unlocked_flag_set_event_handler(void (*handler)());
void set_limit_violation_event_handler(void (*handler)());

// Setters for the Characteristics
void set_sensor_data(sensor_data_t);
void set_battery_level_status(battery_level_status_t);
void set_dip_switch_ip(uint8_t);

#endif
