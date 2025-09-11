package com.fleetops.service;

import com.fleetops.entity.Driver;
import com.fleetops.exception.DriverNotFoundException;
import com.fleetops.repository.DriverRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverService driverService;

    private Driver driver;

    @BeforeEach
    void setUp() {
        driver = new Driver();
        driver.setId(Long.valueOf(1L));
        driver.setName("John Doe");
        driver.setLicenseNumber("LIC123");
    }

    @Nested
    class Create {
        @Test
        void create_WhenValidDriverProvided_ShouldReturnSavedDriver() {
            when(driverRepository.save(any(Driver.class))).thenReturn(driver);
            Driver result = driverService.create(driver);
            assertNotNull(result);
            assertEquals(driver, result);
            verify(driverRepository).save(driver);
            verifyNoMoreInteractions(driverRepository);
        }

        @Test
        void create_WhenRepositoryThrows_ShouldPropagateException() {
            when(driverRepository.save(any(Driver.class))).thenThrow(new RuntimeException("DB error"));
            assertThrows(RuntimeException.class, () -> driverService.create(driver));
            verify(driverRepository).save(driver);
            verifyNoMoreInteractions(driverRepository);
        }

        @ParameterizedTest
        @ValueSource(strings = {"LIC123", "ABC999", "ZXY-000"})
        void create_WhenDifferentLicenseNumbers_ShouldPersistCorrectly(String licenseNumber) {
            driver.setLicenseNumber(licenseNumber);
            when(driverRepository.save(any(Driver.class))).thenReturn(driver);
            Driver result = driverService.create(driver);
            assertEquals(licenseNumber, result.getLicenseNumber());
            verify(driverRepository).save(driver);
            verifyNoMoreInteractions(driverRepository);
        }

        @Test
        void create_WhenDriverIsNull_ShouldThrowNullPointerException() {
            assertThrows(NullPointerException.class, () -> driverService.create(null));
            verifyNoInteractions(driverRepository);
        }
    }

    @Nested
    class Read {
        @Test
        void getAll_WhenRecordsExist_ShouldReturnListOfDrivers() {
            when(driverRepository.findAll()).thenReturn(List.of(driver));
            List<Driver> result = driverService.getAll();
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(driver, result.get(0));
            verify(driverRepository).findAll();
            verifyNoMoreInteractions(driverRepository);
        }

        @Test
        void getAll_WhenNoRecordsExist_ShouldReturnEmptyList() {
            when(driverRepository.findAll()).thenReturn(Collections.emptyList());
            List<Driver> result = driverService.getAll();
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(driverRepository).findAll();
            verifyNoMoreInteractions(driverRepository);
        }

        @Test
        void getById_WhenDriverExists_ShouldReturnDriver() {
            when(driverRepository.findById(Long.valueOf(1L))).thenReturn(Optional.of(driver));
            Driver result = driverService.getById(Long.valueOf(1L));
            assertNotNull(result);
            assertEquals(driver, result);
            verify(driverRepository).findById(Long.valueOf(1L));
            verifyNoMoreInteractions(driverRepository);
        }

        @Test
        void getById_WhenDriverDoesNotExist_ShouldThrowDriverNotFoundException() {
            when(driverRepository.findById(Long.valueOf(1L))).thenReturn(Optional.empty());
            assertThrows(DriverNotFoundException.class, () -> driverService.getById(Long.valueOf(1L)));
            verify(driverRepository).findById(Long.valueOf(1L));
            verifyNoMoreInteractions(driverRepository);
        }

        @Test
        void getById_WhenIdIsNull_ShouldThrowNullPointerException() {
            assertThrows(NullPointerException.class, () -> driverService.getById(null));
            verifyNoInteractions(driverRepository);
        }
    }

    @Nested
    class Update {
        @Test
        void update_WhenDriverExists_ShouldReturnUpdatedDriver() {
            Driver update = new Driver();
            update.setName("Jane Smith");
            update.setLicenseNumber("NEW123");
            when(driverRepository.findById(Long.valueOf(1L))).thenReturn(Optional.of(driver));
            when(driverRepository.save(any(Driver.class))).thenReturn(driver);
            Driver result = driverService.update(Long.valueOf(1L), update);
            assertNotNull(result);
            assertEquals(Long.valueOf(1L), result.getId());
            assertEquals("Jane Smith", result.getName());
            assertEquals("NEW123", result.getLicenseNumber());
            verify(driverRepository).findById(Long.valueOf(1L));
            verify(driverRepository).save(driver);
            verifyNoMoreInteractions(driverRepository);
        }

        @Test
        void update_WhenDriverDoesNotExist_ShouldThrowDriverNotFoundException() {
            when(driverRepository.findById(Long.valueOf(1L))).thenReturn(Optional.empty());
            Driver update = new Driver();
            assertThrows(DriverNotFoundException.class, () -> driverService.update(Long.valueOf(1L), update));
            verify(driverRepository).findById(Long.valueOf(1L));
            verifyNoMoreInteractions(driverRepository);
        }

        @Test
        void update_WhenIdIsNull_ShouldThrowNullPointerException() {
            Driver update = new Driver();
            assertThrows(NullPointerException.class, () -> driverService.update(null, update));
            verifyNoInteractions(driverRepository);
        }

        @Test
        void update_WhenDriverIsNull_ShouldThrowNullPointerException() {
            assertThrows(NullPointerException.class, () -> driverService.update(Long.valueOf(1L), null));
            verifyNoInteractions(driverRepository);
        }

        @Test
        void update_WhenSomeFieldsAreNull_ShouldPreserveExistingValues() {
            Driver existing = new Driver();
            existing.setId(Long.valueOf(1L));
            existing.setName("Existing");
            existing.setLicenseNumber("LIC-OLD");
            when(driverRepository.findById(Long.valueOf(1L))).thenReturn(Optional.of(existing));
            when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));
            Driver patch = new Driver();
            patch.setName(null);
            patch.setLicenseNumber("LIC-NEW");
            Driver result = driverService.update(Long.valueOf(1L), patch);
            assertEquals("Existing", result.getName());
            assertEquals("LIC-NEW", result.getLicenseNumber());
            verify(driverRepository).findById(Long.valueOf(1L));
            verify(driverRepository).save(any(Driver.class));
            verifyNoMoreInteractions(driverRepository);
        }
    }

    @Nested
    class Delete {
        @Test
        void delete_WhenCalled_ShouldInvokeRepositoryDeleteById() {
            when(driverRepository.existsById(Long.valueOf(1L))).thenReturn(true);
            driverService.delete(Long.valueOf(1L));
            verify(driverRepository).existsById(Long.valueOf(1L));
            verify(driverRepository).deleteById(Long.valueOf(1L));
            verifyNoMoreInteractions(driverRepository);
        }

        @Test
        void delete_WhenIdIsNull_ShouldThrowNullPointerException() {
            assertThrows(NullPointerException.class, () -> driverService.delete(null));
            verifyNoInteractions(driverRepository);
        }

        @Test
        void delete_WhenMissing_ShouldThrowDriverNotFoundException() {
            when(driverRepository.existsById(Long.valueOf(999999L))).thenReturn(false);
            assertThrows(DriverNotFoundException.class, () -> driverService.delete(Long.valueOf(999999L)));
            verify(driverRepository).existsById(Long.valueOf(999999L));
            verifyNoMoreInteractions(driverRepository);
        }

        @Test
        void delete_WhenRepositoryThrows_ShouldPropagateException() {
            when(driverRepository.existsById(Long.valueOf(1L))).thenReturn(true);
            doThrow(new RuntimeException("DB error")).when(driverRepository).deleteById(Long.valueOf(1L));
            assertThrows(RuntimeException.class, () -> driverService.delete(Long.valueOf(1L)));
            verify(driverRepository).existsById(Long.valueOf(1L));
            verify(driverRepository).deleteById(Long.valueOf(1L));
            verifyNoMoreInteractions(driverRepository);
        }
    }
}
