package at.ac.uibk.plant_health.controllers.sensor_station_controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import at.ac.uibk.plant_health.models.device.AccessPoint;
import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.models.plant.SensorStationPicture;
import at.ac.uibk.plant_health.models.user.Permission;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.repositories.*;
import at.ac.uibk.plant_health.service.AccessPointService;
import at.ac.uibk.plant_health.service.PersonService;
import at.ac.uibk.plant_health.service.SensorStationService;
import at.ac.uibk.plant_health.util.AuthGenerator;
import at.ac.uibk.plant_health.util.MockAuthContext;
import at.ac.uibk.plant_health.util.StringGenerator;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TestSensorStationController {
	@Autowired
	private SensorStationService sensorStationService;
	@Autowired
	private AccessPointService accessPointService;
	@Autowired
	private SensorRepository sensorRepository;
	@Autowired
	private SensorDataRepository sensorDataRepository;
	@Autowired
	private PersonService personService;
	@Autowired
	private PlantPictureRepository plantPictureRepository;
	@Autowired
	private SensorStationRepository sensorStationRepository;
	@Autowired
	private SensorLimitsRepository sensorLimitsRepository;
	@Autowired
	private WebApplicationContext webApplicationContext;
	@Autowired
	private MockMvc mockMvc;

	private ObjectMapper mapper = new ObjectMapper();
	private Random rand = new Random();

	private final String picture =
			"iVBORw0KGgoAAAANSUhEUgAAAQAAAAEACAIAAADTED8xAAADMElEQVR4nOzVwQnAIBQFQYXff81RUkQCOyDj1YOPnbXWPmeTRef+/3O/OyBjzh3CD95BfqICMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMO0TAAD//2Anhf4QtqobAAAAAElFTkSuQmCC";
	SensorStationPicture plantPicture;
	@Value("${swa.pictures.path}")
	private String picturesPath;

	private Person createUserAndLogin(boolean alsoAdmin, boolean gardener) {
		String username = StringGenerator.username();
		String password = StringGenerator.password();
		Set<GrantedAuthority> permissions = new java.util.HashSet<>(Set.of(Permission.USER));
		if (alsoAdmin) {
			permissions.add(Permission.ADMIN);
		} else if (gardener) {
			permissions.add(Permission.GARDENER);
		}
		Person person = new Person(username, StringGenerator.email(), password, permissions);
		assertTrue(personService.create(person), "Unable to create user");
		return (Person
		) MockAuthContext.setLoggedInUser(personService.login(username, password).orElse(null));
	}

	@Test
	@Order(1)
	void getSensorStations() throws Exception {
		// precondition accessPoint has found and reported multiple sensorStations
		Person person = createUserAndLogin(true, false);
		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");

		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);
		accessPoint.setUnlocked(true);
		accessPoint.setConnected(true);
		accessPointService.save(accessPoint);

		int sensorStationCount = 5;
		for (int i = 0; i < sensorStationCount; i++) {
			String bdAddress = StringGenerator.macAddress();
			SensorStation sensorStation = new SensorStation(bdAddress, 255 - i);
			sensorStation.setName("SensorStation" + i);
			sensorStation.setAccessPoint(accessPoint);
			sensorStation.setDeleted(false);
			sensorStation.setUnlocked(true);
			sensorStation.setConnected(true);
			sensorStationService.save(sensorStation);
		}
		List<SensorStation> sensorStations = sensorStationService.findAll()
													 .stream()
													 .filter(s -> !s.isDeleted() && s.isUnlocked())
													 .toList();
		accessPointService.foundNewSensorStation(accessPoint, sensorStations);

		mockMvc.perform(MockMvcRequestBuilders.get("/get-sensor-stations")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
						status().isOk(), jsonPath("$.sensorStations").exists(),
						jsonPath("$.sensorStations.length()").value(sensorStations.size()),
						jsonPath("$.sensorStations[0].sensorStationId").exists(),
						jsonPath("$.sensorStations[0].roomName").exists(),
						jsonPath("$.sensorStations[0].name").exists()
				);
	}

	@Test
	void getAllSensorStations() throws Exception {
		// precondition accessPoint has found and reported multiple sensorStations
		Person person = createUserAndLogin(true, false);
		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");

		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);

		int sensorStationCount = 5;
		for (int i = 0; i < sensorStationCount; i++) {
			String bdAddress = StringGenerator.macAddress();
			SensorStation sensorStation = new SensorStation(bdAddress, 255 - i);
			sensorStation.setName("SensorStation" + i);
			sensorStation.setAccessPoint(accessPoint);
			sensorStation.setDeleted(false);
			sensorStation.setUnlocked(true);
			sensorStation.setConnected(true);
			sensorStationService.save(sensorStation);
		}
		List<SensorStation> sensorStations =
				sensorStationService.findAll().stream().filter(s -> !s.isDeleted()).toList();
		accessPointService.foundNewSensorStation(accessPoint, sensorStations);

		mockMvc.perform(MockMvcRequestBuilders.get("/get-all-sensor-stations")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
						status().isOk(), jsonPath("$.sensorStations").exists(),
						jsonPath("$.sensorStations.length()").value(sensorStations.size()),
						jsonPath("$.sensorStations[0].sensorStationId").exists(),
						jsonPath("$.sensorStations[0].bdAddress").exists(),
						jsonPath("$.sensorStations[0].roomName").exists(),
						jsonPath("$.sensorStations[0].name").exists(),
						jsonPath("$.sensorStations[0].dipSwitchId").exists(),
						jsonPath("$.sensorStations[0].unlocked").exists(),
						jsonPath("$.sensorStations[0].connected").exists(),
						jsonPath("$.sensorStations[0].deleted").exists()
				);
	}

	@Test
	void getSensorStation() throws Exception {
		// precondition accessPoint has found and reported multiple sensorStations
		Person person = createUserAndLogin(true, false);
		UUID selfAssignedId = UUID.randomUUID();
		accessPointService.register(selfAssignedId, "Office1");

		AccessPoint accessPoint = accessPointService.findBySelfAssignedId(selfAssignedId);

		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 255);
		sensorStation.setAccessPoint(accessPoint);
		sensorStationService.save(sensorStation);
		sensorStation = sensorStationService.findByBdAddress(bdAddress);
		accessPointService.foundNewSensorStation(accessPoint, List.of(sensorStation));

		Map<String, String> sensorMap = new HashMap<>();
		sensorMap.put("TEMPERATURE", "C");
		sensorMap.put("HUMIDITY", "%");
		sensorMap.put("PRESSURE", "hPa");
		sensorMap.put("SOILHUMIDITY", "%");
		sensorMap.put("LIGHTINTENSITY", "lux");
		sensorMap.put("GASPRESSURE", "ppm");

		for (int i = 0; i < sensorMap.size(); i++) {
			Sensor sensor = new Sensor(
					sensorMap.keySet().toArray()[i].toString(),
					sensorMap.values().toArray()[i].toString()
			);
			if (!sensorRepository.findAll().contains(sensor)) {
				sensorRepository.save(sensor);
			}
			sensor = sensorRepository.findByType(sensor.getType()).get();
			SensorData sensorData =
					new SensorData(LocalDateTime.now(), 0, "h", sensor, sensorStation);
			sensorDataRepository.save(sensorData);
		}
		sensorStation.setSensorData(sensorDataRepository.findAll());
		sensorStationService.save(sensorStation);

		mockMvc.perform(MockMvcRequestBuilders.get("/get-sensor-station")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId()))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
						status().isOk(), jsonPath("$.sensorStation").exists(),
						jsonPath("$.sensorStation.sensorStationId").exists(),
						jsonPath("$.sensorStation.bdAddress").exists(),
						jsonPath("$.sensorStation.dipSwitchId").exists(),
						jsonPath("$.sensorStation.name").exists(),
						jsonPath("$.sensorStation.unlocked").exists(),
						jsonPath("$.sensorStation.connected").exists(),
						jsonPath("$.sensorStation.deleted").exists(),
						jsonPath("$.sensorStation.sensorStationPersonReferences").exists(),
						jsonPath("$.sensorStation.sensorStationPersonReferences").isArray(),
						jsonPath("$.sensorStation.sensorStationPictures").exists(),
						jsonPath("$.sensorStation.sensorStationPictures").isArray(),
						jsonPath("$.sensorStation.sensorLimits").exists(),
						jsonPath("$.sensorStation.sensorLimits").isArray(),
						jsonPath("$.sensorStation.sensorLimits[0].timeStamp").exists(),
						jsonPath("$.sensorStation.sensorLimits[0].upperLimit").exists(),
						jsonPath("$.sensorStation.sensorLimits[0].lowerLimit").exists(),
						jsonPath("$.sensorStation.sensorLimits[0].thresholdDuration").exists(),
						jsonPath("$.sensorStation.sensorLimits[0].sensor").exists(),
						jsonPath("$.sensorStation.sensorLimits[0].gardener").exists()

				);
	}

	@Test
	void getSensorStationInfo() throws Exception {
		AccessPoint accessPoint = new AccessPoint(UUID.randomUUID(), "Office1", 50, false);
		accessPointService.save(accessPoint);

		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 1);
		sensorStation.setAccessPoint(accessPoint);
		sensorStation.setName("Rose");
		sensorStationService.save(sensorStation);

		mockMvc.perform(MockMvcRequestBuilders.get("/get-sensor-station-info")
								.contentType(MediaType.APPLICATION_JSON)
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId()))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
						status().isOk(), jsonPath("$.name").exists(),
						jsonPath("$.roomName").exists(),
						jsonPath("$.name").value(sensorStation.getName()),
						jsonPath("$.roomName").value(accessPoint.getRoomName())
				);
	}

	@Test
	void setUnlockedSensorStation() throws Exception {
		Person person = createUserAndLogin(true, false);
		// precondition accessPoint has found and reported at least one sensor station
		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 1);
		sensorStationService.save(sensorStation);
		sensorStation = sensorStationService.findByBdAddress(bdAddress);

		mockMvc.perform(MockMvcRequestBuilders.post("/set-unlocked-sensor-station")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId()))
								.param("unlocked", "true")
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());
		sensorStation = sensorStationService.findByBdAddress(bdAddress);
		assertTrue(sensorStation.isUnlocked());
	}

	@Test
	void assignGardenerToSensorStation() throws Exception {
		Person admin = createUserAndLogin(true, false);
		Person gardener = createUserAndLogin(false, true);

		AccessPoint accessPoint = new AccessPoint(UUID.randomUUID(), "Office1", 50, false);
		accessPointService.save(accessPoint);

		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 4);
		sensorStation.setUnlocked(true);
		sensorStation.setAccessPoint(accessPoint);
		sensorStationService.save(sensorStation);

		mockMvc.perform(MockMvcRequestBuilders.post("/assign-gardener-to-sensor-station")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(admin))
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId()))
								.param("gardenerId", String.valueOf(gardener.getPersonId()))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		sensorStation = sensorStationService.findByBdAddress(bdAddress);
		assertEquals(gardener, sensorStation.getGardener());
	}
	@Test
	void setSensorLimitsAdmin() throws Exception {
		Person person = createUserAndLogin(true, false);
		// precondition accessPoint has found and reported at least one sensor station
		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 4);
		sensorStation.setUnlocked(true);
		sensorStationService.save(sensorStation);
		// add other sensor station to check that only limits of the selected sensor station are
		// changed
		String otherBdAddress = StringGenerator.macAddress();
		SensorStation otherSensorStation = new SensorStation(otherBdAddress, 5);
		sensorStationService.save(otherSensorStation);

		// precondition sensorStation has at least one sensor
		Map<String, String> sensorMap = new HashMap<>();
		sensorMap.put("TEMPERATURE", "C");
		sensorMap.put("HUMIDITY", "%");
		sensorMap.put("PRESSURE", "hPa");
		sensorMap.put("SOILHUMIDITY", "%");
		sensorMap.put("LIGHTINTENSITY", "lux");
		sensorMap.put("GASPRESSURE", "ppm");

		ArrayNode limits = mapper.createArrayNode();

		for (int i = 0; i < sensorMap.size(); i++) {
			Sensor sensor = new Sensor(
					sensorMap.keySet().toArray()[i].toString(),
					sensorMap.values().toArray()[i].toString()
			);
			if (!sensorRepository.findAll().contains(sensor)) {
				sensorRepository.save(sensor);
			}
			ObjectNode sensorJson = mapper.createObjectNode();
			sensorJson.put("type", sensorMap.keySet().toArray()[i].toString());
			sensorJson.put("unit", sensorMap.values().toArray()[i].toString());

			ObjectNode limit = mapper.createObjectNode();
			limit.putPOJO("sensor", sensor);
			limit.put("upperLimit", rand.nextFloat());
			limit.put("lowerLimit", rand.nextFloat());
			limit.put("thresholdDuration", rand.nextInt(1000));
			limits.add(limit);
		}

		mockMvc.perform(MockMvcRequestBuilders.post("/update-sensor-station")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId()))
								.content(limits.toString())
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		Optional<SensorStation> maybeSensorStation =
				sensorStationRepository.findById(sensorStation.getDeviceId());
		if (maybeSensorStation.isEmpty()) {
			fail("SensorStation not found");
		}
		sensorStation = maybeSensorStation.get();
		assertEquals(sensorMap.size(), sensorStation.getSensorLimits().size());

		// check other sensor station
		maybeSensorStation = sensorStationRepository.findById(otherSensorStation.getDeviceId());
		if (maybeSensorStation.isEmpty()) {
			fail("Other SensorStation not found");
		}
		otherSensorStation = maybeSensorStation.get();
		assertEquals(0, otherSensorStation.getSensorLimits().size());
	}

	@Test
	void setSingleSensorLimitAdmin() throws Exception {
		Person person = createUserAndLogin(true, false);
		// precondition accessPoint has found and reported at least one sensor station that is
		// unlocked
		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 4);
		sensorStation.setUnlocked(true);
		sensorStationService.save(sensorStation);

		// precondition sensorStation has at least one sensor
		Map<String, String> sensorMap = new HashMap<>();
		sensorMap.put("TEMPERATURE", "C");
		sensorMap.put("HUMIDITY", "%");
		sensorMap.put("PRESSURE", "hPa");
		sensorMap.put("SOILHUMIDITY", "%");
		sensorMap.put("LIGHTINTENSITY", "lux");
		sensorMap.put("GASPRESSURE", "ppm");

		for (int i = 0; i < sensorMap.size(); i++) {
			Sensor sensor = new Sensor(
					sensorMap.keySet().toArray()[i].toString(),
					sensorMap.values().toArray()[i].toString()
			);
			if (!sensorRepository.findAll().contains(sensor)) {
				sensorRepository.save(sensor);
			}
		}

		// create single limit for test request
		ArrayNode limits = mapper.createArrayNode();
		ObjectNode sensorJson = mapper.createObjectNode();
		sensorJson.put("type", sensorMap.keySet().toArray()[0].toString());
		sensorJson.put("unit", sensorMap.values().toArray()[0].toString());

		ObjectNode limit = mapper.createObjectNode();
		limit.putPOJO("sensor", sensorJson);
		limit.put("upperLimit", rand.nextFloat());
		limit.put("lowerLimit", rand.nextFloat());
		limit.put("thresholdDuration", rand.nextInt(1000));
		limits.add(limit);

		// run request
		mockMvc.perform(MockMvcRequestBuilders.post("/update-sensor-station")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId()))
								.content(limits.toString())
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		// verify that only single limit got updated
		Optional<SensorStation> maybeSensorStation =
				sensorStationRepository.findById(sensorStation.getDeviceId());
		if (maybeSensorStation.isEmpty()) {
			fail("SensorStation not found");
		}
		sensorStation = maybeSensorStation.get();
		assertEquals(1, sensorStation.getSensorLimits().size());
	}

	@Test
	void getSensorStationData() throws Exception {
		// precondition user is logged in and AccessPoint has found at least one sensor station
		Person person = createUserAndLogin(true, false);

		AccessPoint accessPoint = new AccessPoint(UUID.randomUUID(), "Office1", 50, false);
		accessPointService.save(accessPoint);

		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 1);
		accessPointService.foundNewSensorStation(accessPoint, List.of(sensorStation));
		sensorStation = sensorStationService.findByBdAddress(bdAddress);

		Map<String, String> sensorMap = new HashMap<>();
		sensorMap.put("TEMPERATURE", "C");
		sensorMap.put("HUMIDITY", "%");
		sensorMap.put("PRESSURE", "hPa");
		sensorMap.put("SOILHUMIDITY", "%");
		sensorMap.put("LIGHTINTENSITY", "lux");
		sensorMap.put("GASPRESSURE", "ppm");

		List<Sensor> sensors = new ArrayList<>();
		for (int i = 0; i < sensorMap.size(); i++) {
			Sensor sensor = new Sensor(
					sensorMap.keySet().toArray()[i].toString(),
					sensorMap.values().toArray()[i].toString()
			);
			if (!sensorRepository.findAll().contains(sensor))
				sensorRepository.save(sensor);
			else {
				sensor = sensorRepository.findByType(sensor.getType()).get();
			}
			sensors.add(sensor);
		}
		List<SensorData> sensorDataList = new ArrayList<>();
		for (int d = 0; d < 14; d++) {
			for (int i = 0; i < sensorMap.size(); i++) {
				SensorData sensorData = new SensorData(
						LocalDateTime.now().minusDays(d), rand.nextFloat(), "h", sensors.get(i),
						sensorStation

				);
				sensorDataRepository.save(sensorData);
				sensorDataList.add(sensorData);
			}
		}

		sensorStation.setSensorData(sensorDataList);
		sensorStationService.save(sensorStation);

		var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		var from = LocalDateTime.now().minusDays(12).format(formatter);
		var to = LocalDateTime.now().format(formatter);

		mockMvc.perform(MockMvcRequestBuilders.get("/get-sensor-station-data")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId()))
								.param("from", from)
								.param("to", to)
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());
	}

	private void createPicture(SensorStation sensorStation) {
		List<SensorStationPicture> plantPictures = new ArrayList<>();
		try {
			byte[] imageByte = Base64.decodeBase64(picture);
			String picturePath = picturesPath + UUID.randomUUID() + ".png";
			Path path = Paths.get(picturePath);
			plantPicture =
					new SensorStationPicture(sensorStation, picturePath, LocalDateTime.now());
			plantPictureRepository.save(plantPicture);
			Files.createDirectories(path.getParent());
			Files.write(path, imageByte);
			plantPictures.add(plantPicture);
			sensorStation.setSensorStationPictures(plantPictures);
			sensorStationService.save(sensorStation);

		} catch (Exception e) {
			throw new RuntimeException("Error");
		}
	}

	private void deleteAllPictures(SensorStation sensorStation) throws Exception {
		List<SensorStationPicture> pictures = sensorStation.getSensorStationPictures();
		try {
			for (SensorStationPicture picture1 : pictures) {
				Path path = Paths.get(picture1.getPicturePath());
				Files.delete(path);
			}
			sensorStation.setSensorStationPictures(new ArrayList<>());
			sensorStationService.save(sensorStation);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void uploadPicture() throws Exception {
		// precondition accessPoint exists
		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 2);
		sensorStationService.save(sensorStation);

		MockMultipartFile file = new MockMultipartFile(
				"picture", "test.png", MediaType.IMAGE_PNG_VALUE, picture.getBytes()
		);
		mockMvc.perform(MockMvcRequestBuilders.multipart("/upload-sensor-station-picture")
								.file(file)
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId()))
								.contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpectAll(status().isOk());

		sensorStation = sensorStationService.findByBdAddress(bdAddress);
		assertEquals(1, sensorStation.getSensorStationPictures().size());

		deleteAllPictures(sensorStation);
		assertEquals(0, sensorStation.getSensorStationPictures().size());
	}

	@Test
	void getPictures() throws Exception {
		AccessPoint accessPoint = new AccessPoint(UUID.randomUUID(), "Office1", 50, false);
		accessPointService.save(accessPoint);

		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 3);
		sensorStation.setAccessPoint(accessPoint);
		sensorStation.setName("Plant1");
		sensorStationService.save(sensorStation);

		createPicture(sensorStation);

		mockMvc.perform(MockMvcRequestBuilders.get("/get-sensor-station-pictures")
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId()))
								.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
				.andExpectAll(
						status().isOk(), jsonPath("$.pictures").exists(),
						jsonPath("$.pictures").isArray(),
						jsonPath("$.roomName").value(accessPoint.getRoomName()),
						jsonPath("$.plantName").value(sensorStation.getName()),
						jsonPath("$.pictures[0].timeStamp").exists(),
						jsonPath("$.pictures[0].pictureId").exists()
				);

		sensorStation = sensorStationService.findByBdAddress(bdAddress);
		assertEquals(1, sensorStation.getSensorStationPictures().size());
	}

	@Test
	void getSensorStationPicture() throws Exception {
		AccessPoint accessPoint = new AccessPoint(UUID.randomUUID(), "Office1", 50, false);
		accessPointService.save(accessPoint);

		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 3);
		sensorStation.setAccessPoint(accessPoint);
		sensorStation.setName("Plant1");
		sensorStationService.save(sensorStation);

		createPicture(sensorStation);

		mockMvc.perform(MockMvcRequestBuilders.get("/get-sensor-station-picture")
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId()))
								.param("pictureId", String.valueOf(plantPicture.getPictureId()))
								.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_OCTET_STREAM))
				.andExpectAll(status().isOk());

		sensorStation = sensorStationService.findByBdAddress(bdAddress);
		assertEquals(1, sensorStation.getSensorStationPictures().size());
	}

	@Test
	void deletePictureAdmin() throws Exception {
		Person person = createUserAndLogin(true, false);
		// precondition accessPoint has found and reported at least one sensor station
		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 4);
		sensorStationService.save(sensorStation);

		createPicture(sensorStation);

		List<SensorStationPicture> pictures = plantPictureRepository.findAll();
		SensorStationPicture picture1 = pictures.get(0);

		assertEquals(pictures.size(), sensorStation.getSensorStationPictures().size());

		mockMvc.perform(MockMvcRequestBuilders.post("/delete-sensor-station-picture")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("pictureId", String.valueOf(picture1.getPictureId()))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		Optional<SensorStation> maybeSensorStation =
				sensorStationRepository.findById(sensorStation.getDeviceId());
		if (maybeSensorStation.isEmpty()) {
			fail("SensorStation not found");
		}
		sensorStation = maybeSensorStation.get();
		assertEquals(0, sensorStation.getSensorStationPictures().size());
	}

	@Test
	void deleteAllPicturesAdmin() throws Exception {
		Person person = createUserAndLogin(true, false);
		// precondition accessPoint has found and reported at least one sensor station
		String bdAddress = StringGenerator.macAddress();
		SensorStation testSensorStation = new SensorStation(bdAddress, 4);
		sensorStationService.save(testSensorStation);

		int pictureCount = 10;

		List<SensorStationPicture> plantPictures = new ArrayList<>();
		for (int i = 0; i < pictureCount; i++) {
			try {
				byte[] imageByte = Base64.decodeBase64(picture);
				String picturePath = picturesPath + UUID.randomUUID() + ".png";
				Path path = Paths.get(picturePath);
				plantPicture = new SensorStationPicture(
						testSensorStation, picturePath, LocalDateTime.now()
				);
				plantPictureRepository.save(plantPicture);
				Files.createDirectories(path.getParent());
				Files.write(path, imageByte);
				plantPictures.add(plantPicture);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		testSensorStation.setSensorStationPictures(plantPictures);
		sensorStationService.save(testSensorStation);

		List<SensorStationPicture> pictures =
				plantPictureRepository.findAll()
						.stream()
						.filter(p -> p.getSensorStation().getBdAddress().equals(bdAddress))
						.toList();
		assertEquals(pictures.size(), testSensorStation.getSensorStationPictures().size());

		mockMvc.perform(MockMvcRequestBuilders.post("/delete-all-sensor-station-pictures")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId",
									   String.valueOf(testSensorStation.getDeviceId()))
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isOk());

		Optional<SensorStation> maybeSensorStation =
				sensorStationRepository.findById(testSensorStation.getDeviceId());
		if (maybeSensorStation.isEmpty()) {
			fail("SensorStation not found");
		}
		testSensorStation = maybeSensorStation.get();
		assertEquals(0, testSensorStation.getSensorStationPictures().size());
	}

	@Test
	void deleteSensorStation() throws Exception {
		Person person = createUserAndLogin(true, false);
		// precondition accessPoint has found and reported at least one sensor station
		String bdAddress = StringGenerator.macAddress();
		SensorStation sensorStation = new SensorStation(bdAddress, 4);
		sensorStationService.save(sensorStation);

		// run request
		mockMvc.perform(MockMvcRequestBuilders.delete("/delete-sensor-station")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId",
									   String.valueOf(sensorStation.getDeviceId())))
				.andExpectAll(status().isOk());

		// check if sensor station is removed
		assertTrue(sensorStationService.findById(sensorStation.getDeviceId()).isDeleted());
		assertNull(sensorStationService.findById(sensorStation.getDeviceId()).getBdAddress());
	}

	@Test
	void testGetSensorStationWithInvalidId() throws Exception {
		Person person = createUserAndLogin(true, false);
		mockMvc.perform(MockMvcRequestBuilders.get("/get-sensor-station")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId", String.valueOf(UUID.randomUUID())))
				.andExpectAll(status().isNotFound());
	}

	@Test
	void testGetSensorStationInfoWithInvalidId() throws Exception {
		Person person = createUserAndLogin(true, false);
		mockMvc.perform(MockMvcRequestBuilders.get("/get-sensor-station-info")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId", String.valueOf(UUID.randomUUID())))
				.andExpectAll(status().isNotFound());
	}

	@Test
	void testSetUnlockedSensorStationWithInvalidId() throws Exception {
		Person person = createUserAndLogin(true, false);
		mockMvc.perform(MockMvcRequestBuilders.put("/set-unlocked-sensor-station")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId", String.valueOf(UUID.randomUUID()))
								.param("unlocked", String.valueOf(false)))
				.andExpectAll(status().isNotFound());
	}

	@Test
	void testUpdateSensorStationWithInvalidId() throws Exception {
		Person person = createUserAndLogin(true, false);
		mockMvc.perform(MockMvcRequestBuilders.put("/update-sensor-station")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId", String.valueOf(UUID.randomUUID()))
								.param("transferInterval", String.valueOf(10))
								.content("[]")
								.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isNotFound());
	}

	@Test
	void testAssignGardenerToSensorStationWithInvalidId() throws Exception {
		Person person = createUserAndLogin(true, false);
		mockMvc.perform(MockMvcRequestBuilders.post("/assign-gardener-to-sensor-station")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId", String.valueOf(UUID.randomUUID()))
								.param("gardenerId", String.valueOf(UUID.randomUUID())))
				.andExpectAll(status().isNotFound());
	}

	@Test
	void testGetSensorStationDataWithInvalidId() throws Exception {
		Person person = createUserAndLogin(true, false);
		mockMvc.perform(MockMvcRequestBuilders.get("/get-sensor-station-data")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId", String.valueOf(UUID.randomUUID()))
								.param("from", String.valueOf(LocalDateTime.now()))
								.param("to", String.valueOf(LocalDateTime.now())))
				.andExpectAll(status().isNotFound());
	}

	@Test
	void testGetSensorStationPicturesWithInvalidId() throws Exception {
		Person person = createUserAndLogin(true, false);
		mockMvc.perform(MockMvcRequestBuilders.get("/get-sensor-station-pictures")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId", String.valueOf(UUID.randomUUID())))
				.andExpectAll(status().isNotFound());
	}

	@Test
	void testGetSensorStationPictureWithInvalidId() throws Exception {
		Person person = createUserAndLogin(true, false);
		mockMvc.perform(MockMvcRequestBuilders.get("/get-sensor-station-picture")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("pictureId", String.valueOf(UUID.randomUUID())))
				.andExpectAll(status().isNotFound());
	}

	@Test
	void testGetNewsetSensorStationPictureWithInvalidId() throws Exception {
		Person person = createUserAndLogin(true, false);
		mockMvc.perform(MockMvcRequestBuilders.get("/get-newest-sensor-station-picture")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId", String.valueOf(UUID.randomUUID())))
				.andExpectAll(status().isNotFound());
	}

	@Test
	void testDeleteSensorStationPictureWithInvalidId() throws Exception {
		Person person = createUserAndLogin(true, false);
		mockMvc.perform(MockMvcRequestBuilders.post("/delete-sensor-station-picture")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("pictureId", String.valueOf(UUID.randomUUID())))
				.andExpectAll(status().isNotFound());
	}

	@Test
	void testDeleteAllSensorStationPictureWithInvalidId() throws Exception {
		Person person = createUserAndLogin(true, false);
		mockMvc.perform(MockMvcRequestBuilders.post("/delete-all-sensor-station-pictures")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId", String.valueOf(UUID.randomUUID())))
				.andExpectAll(status().isNotFound());
	}

	@Test
	void testDeleteSensorStationWithInvalidId() throws Exception {
		Person person = createUserAndLogin(true, false);
		mockMvc.perform(MockMvcRequestBuilders.delete("/delete-sensor-station")
								.header(HttpHeaders.USER_AGENT, "MockTests")
								.header(HttpHeaders.AUTHORIZATION,
										AuthGenerator.generateToken(person))
								.param("sensorStationId", String.valueOf(UUID.randomUUID())))
				.andExpectAll(status().isNotFound());
	}
}
