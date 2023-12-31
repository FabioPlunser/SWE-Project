#ifndef FLASH_STORAGE_CLASS
#define FLASH_STORAGE_CLASS

#include <Arduino.h>
#include <CompilerFunctions.hpp>
#include <Defines.h>
#include <FlashIAPBlockDevice.h>
#include <cassert>
#include <string>
#include <vector>

/*
	Reference taken from
   https://github.com/petewarden/arduino_nano_ble_write_flash/blob/main/arduino_nano_ble_write_flash.ino.
	Last accessed on 15.06.2023.
*/

// The arduino requires the flash buffer to be aligned to the block size
#define FLASH_BLOCK_SIZE 4096
// We need to align the buffer to the block size
#define FLASH_BUFFER_ALIGN(val)                                 \
	((((val) + ((FLASH_BLOCK_SIZE) -1)) / (FLASH_BLOCK_SIZE)) * \
	 (FLASH_BLOCK_SIZE))
// The size of the aligned buffer in bytes
#define FLASH_BUFFER_SIZE_ALIGNED FLASH_BUFFER_ALIGN(1024)
// The buffer itself, aligned to the block size
alignas(FLASH_BUFFER_SIZE_ALIGNED
) const uint8_t flash_buffer[FLASH_BUFFER_SIZE_ALIGNED] = {};

class FlashStorage {
	private:
		// A struct for a storage block with a start address and size to make it
		// easier to read and write
		struct storageBlock {
				uint32_t startAdress;
				uint32_t sizeBytes;
		};
		// The total size of the flash buffer
		const uint32_t FLASH_BLOCK_START =
			reinterpret_cast<uint32_t>(flash_buffer);
		// The block to store the paired device in
		const struct storageBlock PAIRED_DEVICE_BLOCK = {
			FLASH_BLOCK_START, FLASH_BUFFER_ALIGN(64)};

		FlashIAPBlockDevice * flash;

		// Private constructor to make the class a singleton
		FlashStorage() {
			this->flash = new FlashIAPBlockDevice(
				FLASH_BLOCK_START, FLASH_BUFFER_SIZE_ALIGNED
			);
		}

		~FlashStorage() { delete this->flash; }

	public:
		// Delete copy and move constructors and assignment operators
		FlashStorage & operator=(FlashStorage &) = delete;
		FlashStorage(FlashStorage &)			 = delete;

		// Get the singleton instance of the class
		static FlashStorage * getInstance() {
			static FlashStorage storage;
			return &storage;
		}

		/**
		 * Store the paired device in flash
		 * @param deviceName: The data to store. Must be less than the size of
		 * the buffer
		 */
		void writePairedDevice(const arduino::String & deviceName) {
			assert(deviceName.length() < PAIRED_DEVICE_BLOCK.sizeBytes);
			uint8_t * ramBuffer = new uint8_t[PAIRED_DEVICE_BLOCK.sizeBytes];
			uint32_t lastI		= 0;
			for (uint32_t i = 0; i < deviceName.length(); i++) {
				ramBuffer[i] = deviceName[i];
				lastI		 = i;
			}
			arduino::String emptyString = "";
			emptyString.c_str();
			ramBuffer[lastI + 1] = '\0';
			DEBUG_PRINTF(2, "Will write %s to flash.\n", (char *) ramBuffer);
			DEBUG_PRINTF(
				2, "Write will start at %lu with size %lu.\n",
				PAIRED_DEVICE_BLOCK.startAdress, PAIRED_DEVICE_BLOCK.sizeBytes
			);
			this->flash->init();
			this->flash->erase(0, PAIRED_DEVICE_BLOCK.sizeBytes);
			this->flash->program(ramBuffer, 0, PAIRED_DEVICE_BLOCK.sizeBytes);
			this->flash->deinit();
			delete[] ramBuffer;
		}

		/**
		 * Reads the paired device from flash. If no device is paired the
		 * behaviour is undefined.
		 * @returns The paired device if there was one stored previously.
		 * Otherwise undefined.
		 */
		arduino::String readPairedDevice() {
			uint8_t * ramBuffer = new uint8_t[PAIRED_DEVICE_BLOCK.sizeBytes];
			DEBUG_PRINTF(
				2, "Will read from flash at %lu with size %lu.\n",
				PAIRED_DEVICE_BLOCK.startAdress, PAIRED_DEVICE_BLOCK.sizeBytes
			);
			this->flash->init();
			this->flash->read(ramBuffer, 0, PAIRED_DEVICE_BLOCK.sizeBytes);
			this->flash->deinit();
			// for(int i = 0; i < 128; i++){
			// 	Serial.print(ramBuffer[i]);
			// 	Serial.print(" ");
			// }
			DEBUG_PRINTF(2, "Read %s from flash.\n", (char *) ramBuffer);
			arduino::String pairedDevice;
			DEBUG_PRINTF(
				2, "Size Bytes = %lu\n", PAIRED_DEVICE_BLOCK.sizeBytes
			);
			for (uint32_t i = 0; i < PAIRED_DEVICE_BLOCK.sizeBytes; i++) {
				if (ramBuffer[i] == 0) {
					break;
				}
				pairedDevice += (char) ramBuffer[i];
			}
			DEBUG_PRINTF(2, "Paired Device = %s\n", pairedDevice.c_str());
			delete[] ramBuffer;
			return pairedDevice;
		}

		uint32_t getMaxSize_PairedDevice() {
			return PAIRED_DEVICE_BLOCK.sizeBytes - 1;
		}
};

#endif // FLASH_STORAGE_CLASS