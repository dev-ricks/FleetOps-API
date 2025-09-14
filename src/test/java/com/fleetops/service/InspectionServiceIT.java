package com.fleetops.service;

import com.fleetops.entity.Inspection;
import com.fleetops.exception.InspectionNotFoundException;
import com.fleetops.repository.InspectionRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {"spring.jpa.hibernate" +
                                                                                   ".ddl-auto=create-drop", "spring" +
                                                                                                            ".liquibase.enabled=false", "spring.sql.init.mode=never"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Import(InspectionServiceIT.SaveFailureAspectConfig.class)
class InspectionServiceIT {

    @Autowired
    private InspectionService inspectionService;

    @Autowired
    private InspectionRepository inspectionRepository;

    private Inspection existing;

    @BeforeEach
    void setUp() {
        inspectionRepository.deleteAll();
        existing = new Inspection();
        existing.setInspectionDate(LocalDate.of(2024, 1, 10));
        existing.setStatus("PASSED");
        existing = inspectionRepository.save(existing);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Inspection findFresh(Long id) {
        return inspectionRepository.findById(id).orElseThrow();
    }

    @TestConfiguration
    static class SaveFailureAspectConfig {
        static final AtomicBoolean FAIL_SAVE = new AtomicBoolean(false);

        @Bean
        SaveFailureAspect saveFailureAspect() {
            return new SaveFailureAspect(FAIL_SAVE);
        }
    }

    @org.aspectj.lang.annotation.Aspect
    static class SaveFailureAspect {
        private final AtomicBoolean failSave;

        SaveFailureAspect(AtomicBoolean failSave) {
            this.failSave = failSave;
        }

        @org.aspectj.lang.annotation.Around("execution(* org.springframework.data.repository.CrudRepository+.save(..))")
        public Object aroundSave(org.aspectj.lang.ProceedingJoinPoint pjp) throws Throwable {
            if (failSave.get()) {
                throw new RuntimeException("boom");
            }
            return pjp.proceed();
        }
    }

    @Nested
    class Create {
        @Test
        void create_WhenValid_ShouldCommitAndBeRetrievable() {
            Inspection toCreate = new Inspection();
            toCreate.setInspectionDate(LocalDate.of(2025, 1, 1));
            toCreate.setStatus("PENDING");

            Inspection saved = inspectionService.create(toCreate);
            assertNotNull(saved.getId());

            Optional<Inspection> reloaded = inspectionRepository.findById(saved.getId());
            assertTrue(reloaded.isPresent());
            assertEquals(LocalDate.of(2025, 1, 1), reloaded.get().getInspectionDate());
            assertEquals("PENDING", reloaded.get().getStatus());
        }

        @Test
        void create_WithNullInspection_ShouldThrowException() {
            Inspection inspection = null;
            assertThrows(NullPointerException.class, () -> inspectionService.create(inspection));
        }
    }

    @Nested
    class Read {
        @Test
        void getById_WhenExisting_ShouldReturnWithinReadOnlyTx() {
            Long id = existing.getId();
            Inspection found = inspectionService.getById(id);
            assertNotNull(found);
            assertEquals(id, found.getId());
            assertEquals("PASSED", found.getStatus());
        }

        @Test
        void getAll_WhenCalled_ShouldReturnListWithinReadOnlyTx() {
            List<Inspection> all = inspectionService.getAll();
            assertFalse(all.isEmpty());
            assertTrue(all.stream().anyMatch(i -> "PASSED".equals(i.getStatus())));
        }
    }

    @Nested
    class Update {
        @Test
        void update_ShouldOnlyUpdateNonNullFields() {
            Inspection update = new Inspection();
            update.setStatus("COMPLETED");
            // inspectionDate is intentionally not set
            Inspection result = inspectionService.update(existing.getId(), update);
            assertNotNull(result);
            assertEquals("COMPLETED", result.getStatus());
            assertEquals(LocalDate.of(2024, 1, 10), result.getInspectionDate());
        }

        @Test
        void update_WhenRepositoryThrows_ShouldRollbackAndPreserveState() {
            SaveFailureAspectConfig.FAIL_SAVE.set(true);
            try {
                Inspection patch = new Inspection();
                patch.setInspectionDate(LocalDate.of(2030, 12, 31));
                patch.setStatus("FAILED");

                Long id = existing.getId();
                assertThrows(RuntimeException.class, () -> inspectionService.update(id, patch));

                Inspection reloaded = findFresh(id);
                assertEquals(LocalDate.of(2024, 1, 10), reloaded.getInspectionDate());
                assertEquals("PASSED", reloaded.getStatus());
            } finally {
                SaveFailureAspectConfig.FAIL_SAVE.set(false);
            }
        }

        @Test
        void update_WithNullId_ShouldThrowException() {
            Inspection update = new Inspection();
            assertThrows(NullPointerException.class, () -> inspectionService.update(null, update));
        }

        @Test
        void update_WithNullInspection_ShouldThrowException() {
            assertThrows(NullPointerException.class, () -> inspectionService.update(1L, null));
        }
    }

    @Nested
    class Delete {
        @Test
        void delete_WhenExisting_ShouldRemoveAndCommit() {
            Long id = existing.getId();
            inspectionService.delete(id);
            assertTrue(inspectionRepository.findById(id).isEmpty());
        }

        @Test
        void delete_WhenMissing_ShouldThrowInspectionNotFoundException_AndLeaveStateUnchanged() {
            long beforeCount = inspectionRepository.count();
            Long missingId = 999_999L;
            assertThrows(InspectionNotFoundException.class, () -> inspectionService.delete(missingId));
            assertEquals(beforeCount, inspectionRepository.count());
        }

        @Test
        void delete_ShouldNotFetchEntityBeforeDeletion() {
            inspectionService.delete(existing.getId());
            assertFalse(inspectionRepository.existsById(existing.getId()));
        }

        @Test
        void delete_WithNullId_ShouldThrowException() {
            assertThrows(NullPointerException.class, () -> inspectionService.delete(null));
        }
    }

    @Nested
    class Transactions {
        @Test
        void compositeCreateUpdateDeleteThenFail_WhenFailureOccurs_ShouldRollbackAllChanges() {
            Long updateId = existing.getId();
            Inspection toDelete = new Inspection();
            toDelete.setInspectionDate(LocalDate.of(2024, 2, 20));
            toDelete.setStatus("PENDING");
            toDelete = inspectionRepository.save(toDelete);
            Long deleteId = toDelete.getId();

            long beforeCount = inspectionRepository.count();

            Inspection updateOriginal = findFresh(updateId);
            LocalDate originalDate = updateOriginal.getInspectionDate();
            String originalStatus = updateOriginal.getStatus();

            Inspection toCreate = new Inspection();
            toCreate.setInspectionDate(LocalDate.of(2035, 1, 1));
            toCreate.setStatus("FAILED");

            assertThrows(RuntimeException.class,
                         () -> inspectionService.compositeCreateUpdateDeleteThenFail(toCreate, updateId, deleteId));

            Inspection updateAfter = findFresh(updateId);
            assertEquals(originalDate, updateAfter.getInspectionDate());
            assertEquals(originalStatus, updateAfter.getStatus());
            assertTrue(inspectionRepository.findById(deleteId).isPresent());
            assertEquals(beforeCount, inspectionRepository.count());
        }

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void compositeOperation_WhenExceptionThrown_ShouldRollbackAllChanges() {
            long initialCount = inspectionRepository.count();
            Inspection newInspection = new Inspection();
            newInspection.setInspectionDate(LocalDate.now());
            newInspection.setStatus("NEW");
            assertThrows(RuntimeException.class,
                         () -> inspectionService.compositeCreateUpdateDeleteThenFail(newInspection, existing.getId(),
                                                                                     existing.getId()));
            assertEquals(initialCount, inspectionRepository.count());
            Inspection notUpdated = inspectionRepository.findById(existing.getId()).orElseThrow();
            assertEquals("PASSED", notUpdated.getStatus());
        }
    }
}
