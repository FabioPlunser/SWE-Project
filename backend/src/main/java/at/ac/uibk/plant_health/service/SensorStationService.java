package at.ac.uibk.plant_health.service;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import at.ac.uibk.plant_health.models.device.SensorStation;
import at.ac.uibk.plant_health.models.exceptions.ServiceException;
import at.ac.uibk.plant_health.models.plant.PlantPicture;
import at.ac.uibk.plant_health.models.plant.Sensor;
import at.ac.uibk.plant_health.models.plant.SensorData;
import at.ac.uibk.plant_health.models.plant.SensorLimits;
import at.ac.uibk.plant_health.models.user.Person;
import at.ac.uibk.plant_health.repositories.*;

@Service
public class SensorStationService {
	@Autowired
	private SensorStationRepository sensorStationRepository;
	@Autowired
	private SensorDataRepository sensorDataRepository;
	@Autowired
	private SensorRepository sensorRepository;
	@Autowired
	private PlantPictureRepository plantPictureRepository;
	@Autowired
	private SensorStationPersonReferenceRepository sensorStationPersonReferenceRepository;
	@Autowired
	private SensorLimitsRepository sensorLimitsRepository;
	@Value("${swa.pictures.path}")
	private String picturesPath;

	public SensorStation findById(UUID id) throws ServiceException {
		Optional<SensorStation> maybeSensorStation = this.sensorStationRepository.findById(id);
		if (maybeSensorStation.isEmpty()) {
			throw new ServiceException("Could not find SensorStation", 404);
		}
		return maybeSensorStation.get();
	}

	public List<SensorStation> findAll() {
		return sensorStationRepository.findAll();
	}

	public SensorStation findByBdAddress(String bdAddress) throws ServiceException {
		Optional<SensorStation> maybeSensorStation =
				this.sensorStationRepository.findByBdAddress(bdAddress);
		if (maybeSensorStation.isEmpty()) {
			throw new ServiceException("Could not find SensorStation", 404);
		}
		return maybeSensorStation.get();
	}

	public SensorStation save(SensorStation sensorStation) throws ServiceException {
		try {
			return sensorStationRepository.save(sensorStation);
		} catch (Exception e) {
			throw new ServiceException("Could not save SensorStation", 500);
		}
	}

	/**
	 * Set unlock status of sensor station.
	 * @param unlocked
	 * @param sensorStationId
	 * @return
	 * @throws ServiceException
	 */
	public void setUnlocked(boolean unlocked, UUID sensorStationId) throws ServiceException {
		SensorStation sensorStation = findById(sensorStationId);
		sensorStation.setUnlocked(unlocked);
		save(sensorStation);
	}

	/**
	 * Set sensor limits of sensor station.
	 * @param sensorLimits
	 * @param sensorStationId
	 * @return
	 */
	// @Transactional
	public void setSensorLimits(
			List<SensorLimits> sensorLimits, UUID sensorStationId, Person person
	) throws ServiceException {
		SensorStation sensorStation = findById(sensorStationId);
		for (SensorLimits limit : sensorLimits) {
			Optional<Sensor> sensor = sensorRepository.findByType(limit.getSensor().getType());
			if (sensor.isEmpty())
				throw new ServiceException(
						"Sensor " + limit.getSensor().getType() + " not found", 500
				);
			limit.setSensor(sensor.get());
			limit.setGardener(person);
			limit.setSensorStation(sensorStation);
			limit.setTimeStamp(LocalDateTime.now());
			try {
				sensorLimitsRepository.save(limit);
			} catch (Exception e) {
				throw new ServiceException("Could not save sensor limits", 500);
			}
		}
	}

	/**
	 * Get pictures of SensorStation
	 * @param sensorStationId
	 * @return list of base64 encoded pictures
	 */
	public List<PlantPicture> getPictures(UUID sensorStationId) throws ServiceException {
		SensorStation sensorStation = findById(sensorStationId);
		return sensorStation.getPlantPictures();
	}

	public PlantPicture getPicture(UUID pictureId) throws ServiceException {
		Optional<PlantPicture> maybePicture = plantPictureRepository.findById(pictureId);
		if (maybePicture.isEmpty()) throw new ServiceException("Picture does not exist", 404);
		return maybePicture.get();
	}

	/**
	 * uploadPicture
	 * @param picture base64 encoded picture
	 * @return true if upload was successful
	 */
	public void uploadPicture(MultipartFile picture, UUID sensorStationId) throws ServiceException {
		SensorStation sensorStation = findById(sensorStationId);
		PlantPicture plantPicture = null;
		try {
			String extension = Objects.requireNonNull(picture.getContentType()).split("/")[1];
			String picturePath = picturesPath + UUID.randomUUID() + "." + extension;
			Path path = Paths.get(picturePath);

			plantPicture = new PlantPicture(sensorStation, picturePath, LocalDateTime.now());
			plantPictureRepository.save(plantPicture);

			Files.createDirectories(path.getParent());
			Files.write(path, picture.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException("Could not save picture", 500);
		}

		List<PlantPicture> sensorStationPictures = sensorStation.getPlantPictures();
		sensorStationPictures.add(plantPicture);
		sensorStation.setPlantPictures(sensorStationPictures);
		save(sensorStation);
	}

	/**
	 * Delete specific picture of sensor station
	 * @param pictureId
	 * @return
	 * @throws ServiceException
	 */
	public void deletePicture(UUID pictureId) throws ServiceException {
		Optional<PlantPicture> maybePicture = plantPictureRepository.findById(pictureId);
		if (maybePicture.isEmpty()) throw new ServiceException("Picture does not exist", 404);
		PlantPicture picture = maybePicture.get();
		SensorStation sensorStation = picture.getSensorStation();

		try {
			Path path = Paths.get(picture.getPicturePath());
			Files.delete(path);
			plantPictureRepository.delete(picture);
			sensorStation.getPlantPictures().remove(picture);
			save(sensorStation);
		} catch (Exception e) {
			throw new ServiceException("Failed to delete pictue of the server", 500);
		}
	}

	/**
	 * Delete all pictures of a sensor station
	 * @param sensorStationId
	 * @throws ServiceException
	 */
	public void deleteAllPictures(UUID sensorStationId) throws ServiceException {
		SensorStation sensorStation = findById(sensorStationId);
		List<PlantPicture> pictures = new ArrayList<>(sensorStation.getPlantPictures());
		try {
			for (PlantPicture picture : pictures) {
				try {
					Path path = Paths.get(picture.getPicturePath());
					Files.delete(path);
					plantPictureRepository.delete(picture);
				} catch (Exception e) {
					plantPictureRepository.delete(picture);
					throw new ServiceException("Picture already deleted from server", 500);
				}

				sensorStation.getPlantPictures().remove(picture);
			}
			save(sensorStation);
		} catch (Exception e) {
			throw new ServiceException("Failed to delete picture of the server", 500);
		}
	}

	public void addSensorData(SensorStation sensorStation, SensorData data)
			throws ServiceException {
		if (data == null || sensorStation == null) throw new ServiceException("Invalid data", 400);
		Sensor sensor = data.getSensor();
		Optional<Sensor> maybeSensor = sensorRepository.findByType(sensor.getType());
		if (maybeSensor.isPresent()) {
			sensor = maybeSensor.get();
		} else {
			sensor = sensorRepository.save(sensor);
		}

		data.setSensor(sensor);
		data.setSensorStation(sensorStation);
		this.sensorDataRepository.save(data);
	}

	public void addSensorData(SensorStation sensorStation, List<SensorData> dataList)
			throws ServiceException {
		if (dataList == null || sensorStation == null)
			throw new ServiceException("Invalid data", 400);

		for (SensorData data : dataList) {
			try {
				this.addSensorData(sensorStation, data);
			} catch (ServiceException e) {
				throw new ServiceException("Could not save sensor data", 500);
			}
		}
	}
}