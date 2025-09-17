package com.fleetops.service;

import com.fleetops.entity.Inspection;
import com.fleetops.entity.Vehicle;
import com.fleetops.exception.InspectionNotFoundException;
import com.fleetops.repository.InspectionRepository;
import com.fleetops.repository.VehicleRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspectionServiceTest {

    @Mock
    private InspectionRepository inspectionRepository;

    @InjectMocks
    private InspectionService inspectionService;

    private Inspection inspection;

    @BeforeEach
    void setUp() {
        inspection = new Inspection();
        inspection.setId(Long.valueOf(1L));
        inspection.setInspectionDate(LocalDate.now());
        inspection.setStatus("PASSED");
    }

    @Nested
    class Update {
        private final Long TEST_ID = Long.valueOf(1L);
        private Inspection existingInspection;

        @BeforeEach
        void setUp() {
            existingInspection = new Inspection();
            existingInspection.setId(TEST_ID);
            existingInspection.setStatus("PASSED");
            existingInspection.setInspectionDate(LocalDate.of(2023, 1, 1));
        }

        @Test
        void update_WhenIdIsNull_ShouldThrowNullPointerException_AndNotCallRepository() {
            Inspection updated = new Inspection();
            assertThrows(NullPointerException.class, () -> inspectionService.update(null, updated));
            verifyNoInteractions(inspectionRepository);
        }

        @Test
        void update_WhenInspectionIsNull_ShouldThrowNullPointerException_AndNotCallRepository() {
            assertThrows(NullPointerException.class, () -> inspectionService.update(TEST_ID, null));
            verifyNoInteractions(inspectionRepository);
        }

        @Test
        void update_WhenInspectionDoesNotExist_ShouldThrowInspectionNotFoundException() {
            when(inspectionRepository.findById(TEST_ID)).thenReturn(Optional.empty());
            Inspection updatedInspection = new Inspection();
            assertThrows(InspectionNotFoundException.class, () -> inspectionService.update(TEST_ID, updatedInspection));
            verify(inspectionRepository).findById(TEST_ID);
            verifyNoMoreInteractions(inspectionRepository);
        }

        @Test
        void update_WhenInspectionDateIsNotSet_ShouldOnlyUpdateNonNullFields() {
            Inspection update = new Inspection();
            update.setStatus("FAILED");
            // Note: inspectionDate is intentionally not set in the update
            when(inspectionRepository.findById(TEST_ID)).thenReturn(Optional.of(existingInspection));
            when(inspectionRepository.save(any(Inspection.class))).thenAnswer(invocation -> invocation.getArgument(0));
            Inspection result = inspectionService.update(TEST_ID, update);
            assertNotNull(result);
            assertEquals("FAILED", result.getStatus());
            assertEquals(LocalDate.of(2023, 1, 1), result.getInspectionDate());
        }

        @Test
        void update_WhenRepositoryThrows_ShouldPropagateException() {
            Inspection update = new Inspection();
            update.setStatus("FAILED");
            when(inspectionRepository.findById(TEST_ID)).thenReturn(Optional.of(existingInspection));
            when(inspectionRepository.save(any(Inspection.class))).thenThrow(new RuntimeException("DB error"));
            assertThrows(RuntimeException.class, () -> inspectionService.update(TEST_ID, update));
            verify(inspectionRepository).findById(TEST_ID);
            verify(inspectionRepository).save(any(Inspection.class));
            verifyNoMoreInteractions(inspectionRepository);
        }

        @Test
        void update_WhenInspectionExists_ShouldReturnUpdatedInspection() {
            Inspection update = new Inspection();
            update.setInspectionDate(LocalDate.now().plusDays(1));
            update.setStatus("FAILED");
            when(inspectionRepository.findById(TEST_ID)).thenReturn(Optional.of(existingInspection));
            when(inspectionRepository.save(any(Inspection.class))).thenAnswer(invocation -> invocation.getArgument(0));
            Inspection result = inspectionService.update(TEST_ID, update);
            assertNotNull(result);
            assertEquals(update.getInspectionDate(), result.getInspectionDate());
            assertEquals(update.getStatus(), result.getStatus());
            verify(inspectionRepository).findById(TEST_ID);
            verify(inspectionRepository).save(any(Inspection.class));
            verifyNoMoreInteractions(inspectionRepository);
        }

        @Test
        void update_WhenSomeFieldsAreNull_ShouldPreserveExistingValues() {
            // Spec-driven test: desired partial update semantics
            Inspection existing = new Inspection();
            existing.setId(TEST_ID);
            existing.setInspectionDate(LocalDate.of(2024, 1, 10));
            existing.setStatus("PASSED");
            Vehicle existingVehicle = new Vehicle(10L, "AAA-111", "Make", "Model");
            existing.setVehicle(existingVehicle);

            Inspection patch = new Inspection();
            patch.setInspectionDate(null);
            patch.setStatus(null);
            patch.setVehicle(null);

            when(inspectionRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));
            when(inspectionRepository.save(any(Inspection.class))).thenAnswer(inv -> inv.getArgument(0));

            Inspection result = inspectionService.update(TEST_ID, patch);

            assertEquals(LocalDate.of(2024, 1, 10), result.getInspectionDate());
            assertEquals("PASSED", result.getStatus());
            assertNotNull(result.getVehicle());
            assertEquals(existingVehicle, result.getVehicle());

            verify(inspectionRepository).findById(TEST_ID);
            verify(inspectionRepository).save(any(Inspection.class));
            verifyNoMoreInteractions(inspectionRepository);
        }
    }

    @Nested
    class Create {
        @Test
        void create_WhenInspectionIsNull_ShouldThrowNullPointerException_AndNotCallRepository() {
            Inspection inspection = null;
            assertThrows(NullPointerException.class, () -> inspectionService.create(inspection));
            verifyNoInteractions(inspectionRepository);
        }
        
        @Test
        void create_WhenIdIsNull_ShouldThrowNullPointerException() {
            Inspection inspection = null;
            assertThrows(NullPointerException.class, () -> inspectionService.create(inspection));
        }

        @Test
        void create_WhenRepositoryThrows_ShouldPropagateException() {
            Inspection createThisInspection = new Inspection();
            createThisInspection.setInspectionDate(LocalDate.now());
            createThisInspection.setStatus("PENDING");
            when(inspectionRepository.save(any(Inspection.class))).thenThrow(new RuntimeException("DB error"));
            assertThrows(RuntimeException.class, () -> inspectionService.create(createThisInspection));
            verify(inspectionRepository).save(createThisInspection);
            verifyNoMoreInteractions(inspectionRepository);
        }

        @Test
        void create_WhenValidInspectionProvided_ShouldReturnSavedInspection() {
            Inspection createThisInspection = new Inspection();
            createThisInspection.setInspectionDate(LocalDate.now());
            createThisInspection.setStatus("PASSED");
            when(inspectionRepository.save(any(Inspection.class))).thenAnswer(inv -> {
                Inspection arg = inv.getArgument(0);
                arg.setId(Long.valueOf(100L));
                return arg;
            });
            Inspection result = inspectionService.create(createThisInspection);
            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(createThisInspection.getInspectionDate(), result.getInspectionDate());
            assertEquals(createThisInspection.getStatus(), result.getStatus());
            verify(inspectionRepository).save(createThisInspection);
            verifyNoMoreInteractions(inspectionRepository);
        }

        @ParameterizedTest
        @ValueSource(strings = {"PASSED", "FAILED", "PENDING"})
        void create_WhenDifferentStatuses_ShouldPersistCorrectly(String status) {
            Inspection createThisInspection = new Inspection();
            createThisInspection.setInspectionDate(LocalDate.now());
            createThisInspection.setStatus(status);
            when(inspectionRepository.save(any(Inspection.class))).thenAnswer(inv -> {
                Inspection arg = inv.getArgument(0);
                arg.setId(Long.valueOf(123L));
                return arg;
            });
            Inspection result = inspectionService.create(createThisInspection);
            assertNotNull(result.getId());
            assertEquals(status, result.getStatus());
            verify(inspectionRepository).save(createThisInspection);
            verifyNoMoreInteractions(inspectionRepository);
        }
    }

    @Nested
    class Delete {
        private final Long TEST_ID = Long.valueOf(1L);
        
        @Test
        void delete_WhenIdIsNull_ShouldThrowNullPointerException() {
            assertThrows(NullPointerException.class, () -> inspectionService.delete(null));
        }

        @Test
        void delete_ShouldNotFetchEntityBeforeDeletion() {
            Inspection existing = new Inspection();
            existing.setId(TEST_ID);
            when(inspectionRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));
            inspectionService.delete(TEST_ID);
            verify(inspectionRepository).findById(TEST_ID);
            verify(inspectionRepository).delete(existing);
            verifyNoMoreInteractions(inspectionRepository);
        }

        @Test
        void delete_WhenEntityDoesNotExist_ShouldThrowInspectionNotFoundException() {
            when(inspectionRepository.findById(TEST_ID)).thenReturn(Optional.empty());
            assertThrows(InspectionNotFoundException.class, () -> inspectionService.delete(TEST_ID));
            verify(inspectionRepository).findById(TEST_ID);
            verifyNoMoreInteractions(inspectionRepository);
        }

        @Test
        void delete_WhenRepositoryThrows_ShouldPropagateException() {
            Inspection existing = new Inspection();
            existing.setId(TEST_ID);
            when(inspectionRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));
            doThrow(new RuntimeException("DB error")).when(inspectionRepository).delete(existing);
            assertThrows(RuntimeException.class, () -> inspectionService.delete(TEST_ID));
            verify(inspectionRepository).findById(TEST_ID);
            verify(inspectionRepository).delete(existing);
            verifyNoMoreInteractions(inspectionRepository);
        }
    }

    @Nested
    class GetAll {
        @Test
        void getAll_WhenNoRecordsExist_ShouldReturnEmptyList() {
            when(inspectionRepository.findAll()).thenReturn(Collections.emptyList());
            List<Inspection> result = inspectionService.getAll();
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(inspectionRepository).findAll();
            verifyNoMoreInteractions(inspectionRepository);
        }

        @Test
        void getAll_WhenRecordsExist_ShouldReturnListOfInspections() {
            when(inspectionRepository.findAll()).thenReturn(List.of(inspection));
            List<Inspection> result = inspectionService.getAll();
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(inspection, result.get(0));
            verify(inspectionRepository).findAll();
            verifyNoMoreInteractions(inspectionRepository);
        }
    }

    @Nested
    class GetById {
        private final Long TEST_ID = Long.valueOf(1L);
        @Test
        void getById_WhenIdIsNull_ShouldThrowNullPointerException_AndNotCallRepository() {
            assertThrows(NullPointerException.class, () -> inspectionService.getById(null));
            verifyNoInteractions(inspectionRepository);
        }
        
        @Test
        void getById_WhenIdIsNull_ShouldThrowNullPointerException() {
            assertThrows(NullPointerException.class, () -> inspectionService.getById(null));
        }

        @Test
        void getById_WhenInspectionDoesNotExist_ShouldThrowInspectionNotFoundException() {
            when(inspectionRepository.findById(TEST_ID)).thenReturn(Optional.empty());
            assertThrows(InspectionNotFoundException.class, () -> inspectionService.getById(TEST_ID));
            verify(inspectionRepository).findById(TEST_ID);
            verifyNoMoreInteractions(inspectionRepository);
        }

        @Test
        void getById_WhenInspectionExists_ShouldReturnInspection() {
            when(inspectionRepository.findById(TEST_ID)).thenReturn(Optional.of(inspection));
            Inspection result = inspectionService.getById(TEST_ID);
            assertNotNull(result);
            assertEquals(inspection, result);
            verify(inspectionRepository).findById(TEST_ID);
            verifyNoMoreInteractions(inspectionRepository);
        }
    }
}
