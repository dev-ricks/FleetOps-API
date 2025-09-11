package com.fleetops.service;

import com.fleetops.entity.Vehicle;
import com.fleetops.exception.LicensePlateAlreadyExistsException;
import com.fleetops.exception.VehicleNotFoundException;
import com.fleetops.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

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

    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        vehicle = new Vehicle(Long.valueOf(1L), "ABC-123", "Toyota", "Corolla");
    }

    @Nested
    class Create {
        @Test
        void create_WhenValidVehicle_ShouldReturnSavedVehicle() {
            when(repo.save(any(Vehicle.class))).thenReturn(vehicle);
            Vehicle toCreate = vehicle("ABC-123", "Toyota", "Corolla");
            Vehicle result = vehicleService.create(toCreate);
            assertNotNull(result);
            assertEquals(vehicle, result);
            verify(repo).save(toCreate);
            verifyNoMoreInteractions(repo);
        }

        @Test
        void create_WhenLicensePlateAlreadyExists_ShouldThrowDomainException() {
            Vehicle toCreate = vehicle("DUP-111", "Ford", "Focus");
            when(repo.save(any(Vehicle.class))).thenThrow(new DataIntegrityViolationException("duplicate"));
            assertThrows(LicensePlateAlreadyExistsException.class, () -> vehicleService.create(toCreate));
            verify(repo).save(toCreate);
            verifyNoMoreInteractions(repo);
        }

        @Test
        void create_WhenUnexpectedRepositoryError_ShouldWrapAndPropagate() {
            Vehicle toCreate = vehicle("ERR-500", "Err", "Err");
            when(repo.save(any(Vehicle.class))).thenThrow(new RuntimeException("DB down"));
            RuntimeException ex = assertThrows(RuntimeException.class, () -> vehicleService.create(toCreate));
            assertTrue(ex.getMessage().startsWith("Error creating vehicle:"));
            verify(repo).save(toCreate);
            verifyNoMoreInteractions(repo);
        }

        @ParameterizedTest
        @ValueSource(strings = {"LIC123", "ABC999", "ZXY-000"})
        void create_WhenDifferentLicensePlates_ShouldPersistCorrectly(String plate) {
            Vehicle toCreate = vehicle(plate, "Make", "Model");
            when(repo.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));
            Vehicle result = vehicleService.create(toCreate);
            assertEquals(plate, result.getLicensePlate());
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
            when(repo.findAll()).thenReturn(List.of(vehicle));
            List<Vehicle> result = vehicleService.getAll();
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(vehicle, result.get(0));
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
            when(repo.findById(Long.valueOf(1L))).thenReturn(Optional.of(vehicle));
            Vehicle result = vehicleService.getById(Long.valueOf(1L));
            assertNotNull(result);
            assertEquals(vehicle, result);
            verify(repo).findById(Long.valueOf(1L));
            verifyNoMoreInteractions(repo);
        }

        @Test
        void getById_WhenVehicleDoesNotExist_ShouldThrowVehicleNotFoundException() {
            when(repo.findById(Long.valueOf(1L))).thenReturn(Optional.empty());
            assertThrows(VehicleNotFoundException.class, () -> vehicleService.getById(Long.valueOf(1L)));
            verify(repo).findById(Long.valueOf(1L));
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
        void update_WhenVehicleExists_ShouldReturnUpdatedVehicle() {
            Vehicle updatedData = vehicle("XYZ-999", "Honda", "Civic");
            when(repo.findById(Long.valueOf(1L))).thenReturn(Optional.of(vehicle));
            when(repo.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));
            Vehicle result = vehicleService.update(Long.valueOf(1L), updatedData);
            assertNotNull(result);
            assertEquals(Long.valueOf(1L), result.getId());
            assertEquals("XYZ-999", result.getLicensePlate());
            assertEquals("Honda", result.getMake());
            assertEquals("Civic", result.getModel());
            verify(repo).findById(Long.valueOf(1L));
            verify(repo).save(any(Vehicle.class));
            verifyNoMoreInteractions(repo);
        }

        @Test
        void update_WhenLicensePlateAlreadyExists_ShouldThrowDomainException() {
            Vehicle updatedData = vehicle("DUP-111", "Ford", "Focus");
            when(repo.findById(Long.valueOf(1L))).thenReturn(Optional.of(vehicle));
            when(repo.save(any(Vehicle.class))).thenThrow(new DataIntegrityViolationException("duplicate"));
            assertThrows(LicensePlateAlreadyExistsException.class, () -> vehicleService.update(Long.valueOf(1L), updatedData));
            verify(repo).findById(Long.valueOf(1L));
            verify(repo).save(any(Vehicle.class));
            verifyNoMoreInteractions(repo);
        }

        @Test
        void update_WhenUnexpectedRepositoryError_ShouldWrapAndPropagate() {
            Vehicle updatedData = vehicle("ERR-500", "Err", "Err");
            when(repo.findById(Long.valueOf(1L))).thenReturn(Optional.of(vehicle));
            when(repo.save(any(Vehicle.class))).thenThrow(new RuntimeException("DB down"));
            RuntimeException ex = assertThrows(RuntimeException.class, () -> vehicleService.update(Long.valueOf(1L), updatedData));
            assertTrue(ex.getMessage().startsWith("Error updating vehicle:"));
            verify(repo).findById(Long.valueOf(1L));
            verify(repo).save(any(Vehicle.class));
            verifyNoMoreInteractions(repo);
        }

        @Test
        void update_WhenVehicleDoesNotExist_ShouldThrowVehicleNotFound() {
            when(repo.findById(Long.valueOf(1L))).thenReturn(Optional.empty());
            Vehicle updatedData = vehicle("NOP-000", "Nope", "Nope");
            assertThrows(VehicleNotFoundException.class, () -> vehicleService.update(Long.valueOf(1L), updatedData));
            verify(repo).findById(Long.valueOf(1L));
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
            assertThrows(NullPointerException.class, () -> vehicleService.update(Long.valueOf(1L), null));
            verifyNoInteractions(repo);
        }

        @Test
        void update_WhenSomeFieldsAreNull_ShouldPreserveExistingValues() {
            Vehicle existingVehicle = new Vehicle(Long.valueOf(1L), "ABC-123", "Toyota", "Corolla");
            when(repo.findById(Long.valueOf(1L))).thenReturn(Optional.of(existingVehicle));
            when(repo.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));
            Vehicle patch = vehicle(null, null, "Supra");
            Vehicle result = vehicleService.update(Long.valueOf(1L), patch);
            assertEquals("ABC-123", result.getLicensePlate());
            assertEquals("Toyota", result.getMake());
            assertEquals("Supra", result.getModel());
            verify(repo).findById(Long.valueOf(1L));
            verify(repo).save(any(Vehicle.class));
            verifyNoMoreInteractions(repo);
        }
    }

    @Nested
    class Delete {
        @Test
        void delete_WhenCalled_ShouldInvokeRepositoryDeleteById() {
            when(repo.existsById(Long.valueOf(1L))).thenReturn(true);
            vehicleService.delete(Long.valueOf(1L));
            verify(repo).existsById(Long.valueOf(1L));
            verify(repo).deleteById(Long.valueOf(1L));
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
            when(repo.existsById(Long.valueOf(1L))).thenReturn(true);
            doThrow(new RuntimeException("DB error")).when(repo).deleteById(Long.valueOf(1L));
            assertThrows(RuntimeException.class, () -> vehicleService.delete(Long.valueOf(1L)));
            verify(repo).existsById(Long.valueOf(1L));
            verify(repo).deleteById(Long.valueOf(1L));
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
