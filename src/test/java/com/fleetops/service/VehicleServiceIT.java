package com.fleetops.service;

import com.fleetops.entity.Vehicle;
import com.fleetops.exception.*;
import com.fleetops.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {"spring.jpa.hibernate" +
                                                                                   ".ddl-auto=create-drop", "spring" +
                                                                                                            ".liquibase.enabled=false", "spring.sql.init.mode=never"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Import(VehicleServiceIT.SaveFailureAspectConfig.class)
class VehicleServiceIT {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleRepository vehicleRepository;

    private Vehicle existing;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();
        existing = vehicleRepository.save(vehicle("ABC-123", "Toyota", "Corolla"));
        vehicleRepository.save(vehicle("DUP-111", "Ford",
                                       "Focus")); // allows for some update tests to try duplicate license plate testing
    }

    protected Vehicle findFresh(Long id) {
        entityManager.clear();
        return vehicleRepository.findById(id).orElseThrow();
    }

    private Vehicle vehicle(String plate, String make, String model) {
        Vehicle v = new Vehicle();
        v.setLicensePlate(plate);
        v.setMake(make);
        v.setModel(model);
        return v;
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
                Object[] args = pjp.getArgs();
                if (args != null && args.length == 1 && args[0] instanceof Vehicle v) {
                    String plate = v.getLicensePlate();
                    if (plate != null && plate.startsWith("DUP")) {
                        throw new DataIntegrityViolationException("simulated duplicate");
                    }
                    if (plate != null && plate.startsWith("ERR")) {
                        throw new DataAccessResourceFailureException("simulated repository failure");
                    }
                }
                throw new DataAccessResourceFailureException("simulated repository failure");
            }
            return pjp.proceed();
        }
    }

    @Nested
    class Create {
        @Test
        void create_WhenValid_ShouldCommitAndBeRetrievable() {
            Vehicle toCreate = vehicle("XYZ-999", "Honda", "Civic");

            Vehicle saved = vehicleService.create(toCreate);
            assertNotNull(saved.getId());

            Optional<Vehicle> reloaded = vehicleRepository.findById(saved.getId());
            assertTrue(reloaded.isPresent());
            assertEquals("XYZ-999", reloaded.get().getLicensePlate());
            assertEquals("Honda", reloaded.get().getMake());
            assertEquals("Civic", reloaded.get().getModel());
        }

        @Test
        void create_WhenRepositoryThrowsDuplicate_ShouldMapToDomainException() {
            SaveFailureAspectConfig.FAIL_SAVE.set(true);
            try {
                Vehicle toCreate = vehicle("DUP-111", "Ford", "Focus");
                assertThrows(LicensePlateAlreadyExistsException.class, () -> vehicleService.create(toCreate));
            } finally {
                SaveFailureAspectConfig.FAIL_SAVE.set(false);
            }
        }

        @Test
        void create_WhenUnexpectedError_ShouldWrapAndPropagate() {
            // Simulate an unexpected error by turning on save failure, then expect runtime mapping
            SaveFailureAspectConfig.FAIL_SAVE.set(true);
            try {
                Vehicle toCreate = vehicle("ERR-500", "Err", "Err");
                assertThrows(ServiceException.class, () -> vehicleService.create(toCreate));
            } finally {
                SaveFailureAspectConfig.FAIL_SAVE.set(false);
            }
        }
    }

    @Nested
    class Read {
        @Test
        void getById_WhenExisting_ShouldReturnVehicle() {
            Long id = existing.getId();
            Vehicle v = vehicleService.getById(id);
            assertNotNull(v);
            assertEquals(id, v.getId());
            assertEquals("ABC-123", v.getLicensePlate());
        }

        @Test
        void getById_WhenMissing_ShouldThrowVehicleNotFound() {
            assertThrows(VehicleNotFoundException.class, () -> vehicleService.getById(999_999L));
        }

        @Test
        void getAll_WhenCalled_ShouldReturnList() {
            List<Vehicle> all = vehicleService.getAll();
            assertFalse(all.isEmpty());
            assertTrue(all.stream().anyMatch(v -> "ABC-123".equals(v.getLicensePlate())));
        }
    }

    @Nested
    class Update {
        @Test
        void update_WhenExisting_ShouldPersistChanges() {
            Long id = existing.getId();
            Vehicle patch = vehicle("NEW-123", "Subaru", "Impreza");

            Vehicle saved = vehicleService.update(id, patch);
            assertEquals("NEW-123", saved.getLicensePlate());
            assertEquals("Subaru", saved.getMake());
            assertEquals("Impreza", saved.getModel());

            Vehicle reloaded = findFresh(id);
            assertEquals("NEW-123", reloaded.getLicensePlate());
            assertEquals("Subaru", reloaded.getMake());
            assertEquals("Impreza", reloaded.getModel());
        }

        @Test
        void update_WhenMissing_ShouldThrowVehicleNotFound() {
            Vehicle patch = new Vehicle();
            assertThrows(VehicleNotFoundException.class, () -> vehicleService.update(999_999L, patch));
        }

        @Test
        void update_WhenRepositoryThrowsDuplicate_ShouldMapToDomainException_AndRollback() {
            SaveFailureAspectConfig.FAIL_SAVE.set(true);
            try {
                Long id = existing.getId();
                Vehicle patch = vehicle("DUP-111", "Ford", "Focus");
                assertThrows(LicensePlateAlreadyExistsException.class, () -> vehicleService.update(id, patch));

                Vehicle reloaded = findFresh(id);
                assertEquals("ABC-123", reloaded.getLicensePlate());
                assertEquals("Toyota", reloaded.getMake());
                assertEquals("Corolla", reloaded.getModel());
            } finally {
                SaveFailureAspectConfig.FAIL_SAVE.set(false);
            }
        }

        @Test
        void update_WhenUnexpectedRepositoryError_ShouldWrapInServiceException_AndPropagate() {
            SaveFailureAspectConfig.FAIL_SAVE.set(true);
            try {
                Long id = existing.getId();
                Vehicle patch = new Vehicle();
                patch.setLicensePlate("ERR-500");
                assertThrows(ServiceException.class, () -> vehicleService.update(id, patch));
            } finally {
                SaveFailureAspectConfig.FAIL_SAVE.set(false);
            }
        }
    }

    @Nested
    class Delete {
        @Test
        void delete_WhenExisting_ShouldRemove() {
            Long id = existing.getId();
            vehicleService.delete(id);
            assertTrue(vehicleRepository.findById(id).isEmpty());
        }

        @Test
        void delete_WhenMissing_ShouldThrowVehicleNotFoundException_AndLeaveStateUnchanged() {
            long before = vehicleRepository.count();
            assertThrows(VehicleNotFoundException.class, () -> vehicleService.delete(999_999L));
            assertEquals(before, vehicleRepository.count());
        }
    }
}
