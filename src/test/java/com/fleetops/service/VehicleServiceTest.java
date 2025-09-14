package com.fleetops.service;

import com.fleetops.entity.Vehicle;
import com.fleetops.exception.LicensePlateAlreadyExistsException;
import com.fleetops.exception.VehicleNotFoundException;
import com.fleetops.repository.VehicleRepository;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository repo;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle baseSavedVehicle;
    private Long baseSavedVehicleId;

    @BeforeEach
    void setUp() {
        baseSavedVehicleId = NumberUtils.LONG_ONE;
        baseSavedVehicle = new Vehicle(baseSavedVehicleId, "ABC-123", "Toyota", "Corolla");
    }

    @Nested
    class Create {
        @Test
        void create_ShouldNormalizeLicensePlateBeforeChecksAndSave() {
            Vehicle toCreate = vehicle(" ab-123 ", " Make ", " Model ");
            // After normalization, plate becomes "AB-123"
            when(repo.existsByLicensePlate("AB-123")).thenReturn(false);
            when(repo.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

            Vehicle result = vehicleService.create(toCreate);

            assertEquals("AB-123", result.getLicensePlate());
            verify(repo).existsByLicensePlate("AB-123");
            // ensure saved entity license plate is normalized
            verify(repo).save(argThat(v -> "AB-123".equals(v.getLicensePlate())));
            verifyNoMoreInteractions(repo);
        }
        @Test
        void create_WhenValidVehicle_ShouldReturnSavedVehicle() {
            when(repo.existsByLicensePlate("ABC-123")).thenReturn(false);
            when(repo.save(any(Vehicle.class))).thenReturn(baseSavedVehicle);
            Vehicle toCreate = vehicle("ABC-123", "Toyota", "Corolla");
            Vehicle result = vehicleService.create(toCreate);
            assertNotNull(result);
            assertEquals(baseSavedVehicle, result);
            verify(repo).existsByLicensePlate("ABC-123");
            verify(repo).save(toCreate);
            verifyNoMoreInteractions(repo);
        }

        @Test
        void create_WhenLicensePlateAlreadyExists_ShouldThrowDomainException() {
            Vehicle toCreate = vehicle("DUP-111", "Ford", "Focus");
            when(repo.existsByLicensePlate("DUP-111")).thenReturn(true);
            assertThrows(LicensePlateAlreadyExistsException.class, () -> vehicleService.create(toCreate));
            verify(repo).existsByLicensePlate("DUP-111");
            verify(repo, never()).save(any(Vehicle.class));
            verifyNoMoreInteractions(repo);
        }

        @Test
        void create_WhenUnexpectedRepositoryError_ShouldWrapAndPropagate() {
            Vehicle toCreate = vehicle("ERR-500", "Err", "Err");
            when(repo.existsByLicensePlate("ERR-500")).thenReturn(false);
            when(repo.save(any(Vehicle.class))).thenThrow(new DataAccessResourceFailureException("DB down"));
            RuntimeException ex = assertThrows(RuntimeException.class, () -> vehicleService.create(toCreate));
            assertEquals("Error creating vehicle", ex.getMessage());
            verify(repo).existsByLicensePlate("ERR-500");
            verify(repo).save(toCreate);
            verifyNoMoreInteractions(repo);
        }

        @ParameterizedTest
        @ValueSource(strings = {"LIC123", "ABC999", "ZXY-000"})
        void create_WhenDifferentLicensePlates_ShouldPersistCorrectly(String plate) {
            Vehicle toCreate = vehicle(plate, "Make", "Model");
            when(repo.existsByLicensePlate(plate)).thenReturn(false);
            when(repo.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));
            Vehicle result = vehicleService.create(toCreate);
            assertEquals(plate, result.getLicensePlate());
            verify(repo).existsByLicensePlate(plate);
            verify(repo).save(toCreate);
            verifyNoMoreInteractions(repo);
        }

        @Test
        void create_WhenVehicleIsNull_ShouldThrowNullPointerException() {
            assertThrows(NullPointerException.class, () -> vehicleService.create(null));
            verifyNoInteractions(repo);
        }
    }

    @Nested
    class Read {
        @Test
        void getAll_WhenRecordsExist_ShouldReturnListOfVehicles() {
            when(repo.findAll()).thenReturn(List.of(baseSavedVehicle));
            List<Vehicle> result = vehicleService.getAll();
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(baseSavedVehicle, result.get(0));
            verify(repo).findAll();
            verifyNoMoreInteractions(repo);
        }

        @Test
        void getAll_WhenNoRecordsExist_ShouldReturnEmptyList() {
            when(repo.findAll()).thenReturn(Collections.emptyList());
            List<Vehicle> result = vehicleService.getAll();
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(repo).findAll();
            verifyNoMoreInteractions(repo);
        }

        @Test
        void getById_WhenVehicleExists_ShouldReturnVehicle() {
            when(repo.findById(baseSavedVehicleId)).thenReturn(Optional.of(baseSavedVehicle));
            Vehicle result = vehicleService.getById(baseSavedVehicleId);
            assertNotNull(result);
            assertEquals(baseSavedVehicle, result);
            verify(repo).findById(baseSavedVehicleId);
            verifyNoMoreInteractions(repo);
        }

        @Test
        void getById_WhenVehicleDoesNotExist_ShouldThrowVehicleNotFoundException() {
            when(repo.findById(baseSavedVehicleId)).thenReturn(Optional.empty());
            assertThrows(VehicleNotFoundException.class, () -> vehicleService.getById(baseSavedVehicleId));
            verify(repo).findById(baseSavedVehicleId);
            verifyNoMoreInteractions(repo);
        }

        @Test
        void getById_WhenIdIsNull_ShouldThrowNullPointerException() {
            assertThrows(NullPointerException.class, () -> vehicleService.getById(null));
            verifyNoInteractions(repo);
        }
    }

    @Nested
    class Update {
        @Test
        void update_ShouldNormalizeInputsAndUseNormalizedForDuplicateCheck() {
            Vehicle existing = new Vehicle(baseSavedVehicleId, "ABC-123", "Toyota", "Corolla");
            when(repo.findById(baseSavedVehicleId)).thenReturn(Optional.of(existing));

            Vehicle patch = vehicle(" xy-999 ", "  NewMake  ", " NewModel ");
            // normalization should call existsByLicensePlateAndIdNot with uppercased/trimmed
            when(repo.existsByLicensePlateAndIdNot("XY-999", baseSavedVehicleId)).thenReturn(false);
            when(repo.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

            Vehicle result = vehicleService.update(baseSavedVehicleId, patch);

            assertEquals("XY-999", result.getLicensePlate());
            assertEquals("NewMake", result.getMake());
            assertEquals("NewModel", result.getModel());
            verify(repo).findById(baseSavedVehicleId);
            verify(repo).existsByLicensePlateAndIdNot("XY-999", baseSavedVehicleId);
            verify(repo).save(argThat(v ->
                    "XY-999".equals(v.getLicensePlate()) &&
                    "NewMake".equals(v.getMake()) &&
                    "NewModel".equals(v.getModel())));
            verifyNoMoreInteractions(repo);
        }
        @Test
        void update_WhenVehicleExists_ShouldReturnUpdatedVehicle() {
            Vehicle updateData = vehicle("XYZ-999", "Honda", "Civic");
            Vehicle updatedVehicle = new Vehicle(baseSavedVehicleId, "XYZ-999", "Honda", "Civic");
            when(repo.findById(baseSavedVehicleId)).thenReturn(Optional.of(baseSavedVehicle));
            when(repo.existsByLicensePlateAndIdNot("XYZ-999", baseSavedVehicleId)).thenReturn(false);
            when(repo.save(any(Vehicle.class))).thenAnswer(inv -> {
                Vehicle arg = inv.getArgument(0);
                arg.setId(baseSavedVehicleId);
                return arg;
            });
            Vehicle result = vehicleService.update(baseSavedVehicleId, updateData);
            assertNotNull(result);
            assertEquals(baseSavedVehicleId, result.getId());
            assertEquals("XYZ-999", result.getLicensePlate());
            assertEquals("Honda", result.getMake());
            assertEquals("Civic", result.getModel());
            verify(repo).findById(baseSavedVehicleId);
            verify(repo).save(any(Vehicle.class));
            verifyNoMoreInteractions(repo);
        }

        @Test
        void update_WhenLicensePlateAlreadyExists_ShouldThrowDomainException() {
            Vehicle updatedData = vehicle("DUP-111", "Ford", "Focus");
            when(repo.findById(baseSavedVehicleId)).thenReturn(Optional.of(baseSavedVehicle));
            when(repo.existsByLicensePlateAndIdNot("DUP-111", baseSavedVehicleId)).thenReturn(true);
            assertThrows(LicensePlateAlreadyExistsException.class, () -> vehicleService.update(baseSavedVehicleId, updatedData));
            verify(repo).findById(baseSavedVehicleId);
            verify(repo).existsByLicensePlateAndIdNot("DUP-111", baseSavedVehicleId);
            verify(repo, never()).save(any(Vehicle.class));
            verifyNoMoreInteractions(repo);
        }

        @Test
        void update_WhenUnexpectedRepositoryError_ShouldWrapAndPropagate() {
            Vehicle updatedData = vehicle("ERR-500", "Err", "Err");
            when(repo.findById(baseSavedVehicleId)).thenReturn(Optional.of(baseSavedVehicle));
            when(repo.existsByLicensePlateAndIdNot("ERR-500", baseSavedVehicleId)).thenReturn(false);
            when(repo.save(any(Vehicle.class))).thenThrow(new DataAccessResourceFailureException("DB down"));
            RuntimeException ex = assertThrows(RuntimeException.class, () -> vehicleService.update(baseSavedVehicleId, updatedData));
            assertEquals("Error updating vehicle", ex.getMessage());
            verify(repo).findById(baseSavedVehicleId);
            verify(repo).existsByLicensePlateAndIdNot("ERR-500", baseSavedVehicleId);
            verify(repo).save(any(Vehicle.class));
            verifyNoMoreInteractions(repo);
        }

        @Test
        void update_WhenVehicleDoesNotExist_ShouldThrowVehicleNotFound() {
            when(repo.findById(baseSavedVehicleId)).thenReturn(Optional.empty());
            Vehicle updatedData = vehicle("NOP-000", "Nope", "Nope");
            assertThrows(VehicleNotFoundException.class, () -> vehicleService.update(baseSavedVehicleId, updatedData));
            verify(repo).findById(baseSavedVehicleId);
            verifyNoMoreInteractions(repo);
        }

        @Test
        void update_WhenIdIsNull_ShouldThrowNullPointerException() {
            Vehicle updatedData = vehicle("XYZ-999", "Honda", "Civic");
            assertThrows(NullPointerException.class, () -> vehicleService.update(null, updatedData));
            verifyNoInteractions(repo);
        }

        @Test
        void update_WhenVehicleIsNull_ShouldThrowNullPointerException() {
            assertThrows(NullPointerException.class, () -> vehicleService.update(baseSavedVehicleId, null));
            verifyNoInteractions(repo);
        }

        @Test
        void update_WhenSomeFieldsAreNull_ShouldPreserveExistingValues() {
            Vehicle existingVehicle = new Vehicle(baseSavedVehicleId, "ABC-123", "Toyota", "Corolla");
            when(repo.findById(baseSavedVehicleId)).thenReturn(Optional.of(existingVehicle));
            when(repo.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));
            Vehicle patch = vehicle(null, null, "Supra");
            Vehicle result = vehicleService.update(baseSavedVehicleId, patch);
            assertEquals("ABC-123", result.getLicensePlate());
            assertEquals("Toyota", result.getMake());
            assertEquals("Supra", result.getModel());
            verify(repo).findById(baseSavedVehicleId);
            verify(repo).save(any(Vehicle.class));
            verifyNoMoreInteractions(repo);
        }
    }

    @Nested
    class Delete {
        @Test
        void delete_WhenCalled_ShouldInvokeRepositoryDeleteById() {
            when(repo.existsById(baseSavedVehicleId)).thenReturn(true);
            vehicleService.delete(baseSavedVehicleId);
            verify(repo).existsById(baseSavedVehicleId);
            verify(repo).deleteById(baseSavedVehicleId);
            verifyNoMoreInteractions(repo);
        }

        @Test
        void delete_WhenIdIsNull_ShouldThrowNullPointerException() {
            assertThrows(NullPointerException.class, () -> vehicleService.delete(null));
            verifyNoInteractions(repo);
        }

        @Test
        void delete_WhenMissing_ShouldThrowVehicleNotFoundException() {
            when(repo.existsById(Long.valueOf(999999L))).thenReturn(false);
            assertThrows(VehicleNotFoundException.class, () -> vehicleService.delete(Long.valueOf(999999L)));
            verify(repo).existsById(Long.valueOf(999999L));
            verifyNoMoreInteractions(repo);
        }

        @Test
        void delete_WhenRepositoryThrows_ShouldPropagateException() {
            when(repo.existsById(baseSavedVehicleId)).thenReturn(true);
            doThrow(new RuntimeException("DB error")).when(repo).deleteById(baseSavedVehicleId);
            assertThrows(RuntimeException.class, () -> vehicleService.delete(baseSavedVehicleId));
            verify(repo).existsById(baseSavedVehicleId);
            verify(repo).deleteById(baseSavedVehicleId);
            verifyNoMoreInteractions(repo);
        }
    }

    private Vehicle vehicle(String plate, String make, String model) {
        Vehicle v = new Vehicle();
        v.setLicensePlate(plate);
        v.setMake(make);
        v.setModel(model);
        return v;
    }
}
