package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.*;

import at.ac.uibk.plant_health.models.SensorStationPersonReference;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class UserSensorStationsResponse extends RestResponse implements Serializable {
	private final List<InnerResponse> sensorStations;
	public UserSensorStationsResponse(List<SensorStation> sensorStations, Person person) {
		if (person.getPermissions().contains(Permission.GARDENER)) {
			this.sensorStations =
					sensorStations.stream()
							.filter(s -> !s.isDeleted() && s.isUnlocked())
							.filter(s -> {
								if (s.getGardener() != null)
									return s.getGardener().equals(person);
								else
									return false;
							})
							.filter(s
									-> person.getSensorStationPersonReferences().stream().noneMatch(
											r -> r.getSensorStation().equals(s)
									))
							.map(InnerResponse::new)
							.toList();
		} else {
			this.sensorStations =
					sensorStations.stream()
							.filter(s -> !s.isDeleted() && s.isUnlocked())
							.filter(s
									-> person.getSensorStationPersonReferences().stream().noneMatch(
											r -> r.getSensorStation().equals(s)
									))
							.map(InnerResponse::new)
							.toList();
		}
	}

	@Getter
	private static class InnerResponse implements Serializable {
		private final UUID sensorStationId;
		private final String roomName;
		private final String sensorStationName;
		public InnerResponse(SensorStation sensorStation) {
			this.sensorStationId = sensorStation.getDeviceId();
			this.roomName = sensorStation.getAccessPoint().getRoomName();
			this.sensorStationName = sensorStation.getName();
		}
	}
}
