#ifndef ERROR_HANDLER_CLASS
#define ERROR_HANDLER_CLASS

#include "../../include/Defines.h"
#include "LedController.cpp"
#include "NotificationQueue.cpp"
#include "SensorError.cpp"
#include "SensorErrors.h"

class NotificationHandler {
	private:
		NotificationQueue * notificationQueue;
		LedHandler * ledHandler;

		NotificationHandler(
			uint8_t ledPinRed, uint8_t ledPinGreen, uint8_t ledPinBlue
		) {
			notificationQueue = &NotificationQueue::getNotificationQueue();
			ledHandler		  = &LedHandler::getErrorLedHandler(
				   ledPinRed, ledPinGreen, ledPinBlue
			   );
		}

		struct LedErrorParameter {
				uint16_t * ledOnMs;
				uint16_t * ledOffMs;
				uint8_t size;
				uint8_t rgbColorRed;
				uint8_t rgbColorGreen;
				uint8_t rgbColorBlue;
		} ledErrorParam = {0};

	public:
		static NotificationHandler & geterrorHandler(
			uint8_t ledPinRed, uint8_t ledPinGreen, uint8_t ledPinBlue
		) {
			static NotificationHandler errorHandler(
				ledPinRed, ledPinGreen, ledPinBlue
			);
			return errorHandler;
		}

	public:
		void addErrorCode(
			SensorErrors::Type errorType, SensorErrors::Status errorStatus
		) {
			int priority = -1;
			switch (errorType) {
				case SensorErrors::Type::AirHumidityError:
					priority = errorStatus == SensorErrors::Status::High
								   ? AIR_HUMIDITY_TO_HIGH_PRIORITY
								   : AIR_HUMIDITY_TO_LOW_PRIORITY;
					break;
				case SensorErrors::Type::AirPressureError:
					priority = errorStatus == SensorErrors::Status::High
								   ? AIR_TEMPERATURE_TO_HIGH_PRIORITY
								   : AIR_TEMPERATURE_TO_LOW_PRIORITY;
					break;
				case SensorErrors::Type::AirQualityError:
					priority = errorStatus == SensorErrors::Status::High
								   ? AIR_PRESSURE_TO_HIGH_PRIORITY
								   : AIR_PRESSURE_TO_LOW_PRIORITY;
					break;
				case SensorErrors::Type::AirTemperatureError:
					priority = errorStatus == SensorErrors::Status::High
								   ? AIR_QUALITY_TO_HIGH_PRIORITY
								   : AIR_QUALITY_TO_LOW_PRIORITY;
					break;
				case SensorErrors::Type::SoilHumidityError:
					priority = errorStatus == SensorErrors::Status::High
								   ? SOIL_HUMIDITY_TO_HIGH_PRIORITY
								   : SOIL_HUMIDITY_TO_LOW_PRIORITY;
					break;
				case SensorErrors::Type::LightIntensityError:
					priority = errorStatus == SensorErrors::Status::High
								   ? AIR_HUMIDITY_TO_HIGH_PRIORITY
								   : AIR_HUMIDITY_TO_LOW_PRIORITY;
					break;
				default:
					break;
			}
			SensorError error(errorType, errorStatus, priority);
			notificationQueue->addError(error);
		}
};

#endif