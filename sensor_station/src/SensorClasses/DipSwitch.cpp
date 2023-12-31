#ifndef DIP_SWITCH_CLASS
#define DIP_SWITCH_CLASS

#include <Arduino.h>
#include <Defines.h>

class DipSwitchClass {
	private:
		uint8_t pinArraySize;
		uint8_t * pins;

	public:
		/**
		 * @param pins: Expects the connection of the pins, with the first one
		 * being the lowest bit and the last one the highest.
		 * @param pinArraySize: The number of pins the DipSwitch has (max 64)
		 */
		DipSwitchClass(uint8_t * pins, uint8_t pinArraySize) {
			this->pinArraySize = pinArraySize;
			this->pins =
				(uint8_t *) malloc(sizeof(uint8_t) * this->pinArraySize);
			for (int i = 0; i < this->pinArraySize; i++) {
				this->pins[i] = pins[i];
			}
		}

		~DipSwitchClass() { free(pins); }

		/**
		 * Returns the currently set dip switch value from the provided pins at
		 * construction. If no dip switch is connected, the behavior is
		 * undefined.
		 */
		uint64_t getdipSwitchValue() {
			uint64_t currentStatus = 0;
			for (int i = 0; i < this->pinArraySize; i++) {
				currentStatus |= digitalRead(pins[i]) << i;
			}
			return currentStatus;
		}
};

#endif