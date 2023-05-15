package at.ac.uibk.plant_health.models.rest_responses;

import java.io.Serializable;
import java.util.*;

import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.user.Person;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class AdminDashBoardResponse extends RestResponse implements Serializable {
	private final int numOfUsers;
	private final int numOfConnectedSensorStations;
	private final int numOfConnectedAccessPoints;

	public AdminDashBoardResponse(
			List<SensorStation> sensorStationList, List<AccessPoint> accessPointList,
			List<Person> personList
	) {
		this.numOfUsers = personList.size();
		this.numOfConnectedSensorStations =
				(int) sensorStationList.stream().filter(SensorStation::isConnected).count();
		this.numOfConnectedAccessPoints =
				(int) accessPointList.stream().filter(AccessPoint::isConnected).count();
	}
}