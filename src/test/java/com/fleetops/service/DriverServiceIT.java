package com.fleetops.service;

import com.fleetops.entity.Driver;
import com.fleetops.exception.DriverNotFoundException;
import com.fleetops.repository.DriverRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {"spring.jpa.hibernate" +
                                                                                   ".ddl-auto=create-drop", "spring" +
                                                                                                            ".liquibase.enabled=false", "spring.sql.init.mode=never"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Import(DriverServiceIT.SaveFailureAspectConfig.class)
class DriverServiceIT {

    @Autowired
    private DriverService driverService;

    @Autowired
    private DriverRepository driverRepository;

    private Driver existing;

    @BeforeEach
    void setUp() {
        driverRepository.deleteAll();
        existing = driverRepository.save(driver("John Doe", "LIC-123"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Driver findFresh(Long id) {
        return driverRepository.findById(id).orElseThrow();
    }

    private Driver driver(String name, String license) {
        Driver d = new Driver();
        d.setName(name);
        d.setLicenseNumber(license);
        return d;
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
            Driver toCreate = driver("Jane Smith", "NEW-999");
            Driver saved = driverService.create(toCreate);
            assertNotNull(saved.getId());
            Optional<Driver> reloaded = driverRepository.findById(saved.getId());
            assertTrue(reloaded.isPresent());
            assertEquals("Jane Smith", reloaded.get().getName());
            assertEquals("NEW-999", reloaded.get().getLicenseNumber());
        }

        @Test
        void create_WhenNull_ShouldThrowNullPointerException() {
            assertThrows(NullPointerException.class, () -> driverService.create(null));
        }

        @Test
        void create_WhenRepositoryThrows_ShouldPropagateException() {
            SaveFailureAspectConfig.FAIL_SAVE.set(true);
            try {
                Driver toCreate = driver("Err", "ERR-500");
                assertThrows(RuntimeException.class, () -> driverService.create(toCreate));
            } finally {
                SaveFailureAspectConfig.FAIL_SAVE.set(false);
            }
        }
    }

    @Nested
    class Read {
        @Test
        void getById_WhenExisting_ShouldReturn() {
            Long id = existing.getId();
            Driver found = driverService.getById(id);
            assertNotNull(found);
            assertEquals(id, found.getId());
            assertEquals("John Doe", found.getName());
        }

        @Test
        void getById_WhenMissing_ShouldThrowDriverNotFoundException() {
            assertThrows(DriverNotFoundException.class, () -> driverService.getById(999_999L));
        }

        @Test
        void getById_WhenNull_ShouldThrowNullPointerException() {
            assertThrows(NullPointerException.class, () -> driverService.getById(null));
        }

        @Test
        void getAll_WhenCalled_ShouldReturnList() {
            List<Driver> all = driverService.getAll();
            assertFalse(all.isEmpty());
            assertTrue(all.stream().anyMatch(d -> "John Doe".equals(d.getName())));
        }
    }

    @Nested
    class Update {
        @Test
        void update_ShouldOnlyUpdateNonNullFields() {
            Driver patch = driver(null, "LIC-777");
            Driver result = driverService.update(existing.getId(), patch);
            assertEquals("John Doe", result.getName());
            assertEquals("LIC-777", result.getLicenseNumber());
        }

        @Test
        void update_WhenRepositoryThrows_ShouldRollbackAndPreserveState() {
            SaveFailureAspectConfig.FAIL_SAVE.set(true);
            try {
                Long id = existing.getId();
                Driver patch = driver("Changed", "CH-1");
                assertThrows(RuntimeException.class, () -> driverService.update(id, patch));
                Driver reloaded = findFresh(id);
                assertEquals("John Doe", reloaded.getName());
                assertEquals("LIC-123", reloaded.getLicenseNumber());
            } finally {
                SaveFailureAspectConfig.FAIL_SAVE.set(false);
            }
        }

        @Test
        void update_WithNullId_ShouldThrowException() {
            assertThrows(NullPointerException.class, () -> driverService.update(null, driver("x","y")));
        }

        @Test
        void update_WithNullDriver_ShouldThrowException() {
            assertThrows(NullPointerException.class, () -> driverService.update(existing.getId(), null));
        }

        @Test
        void update_WhenMissing_ShouldThrowDriverNotFoundException() {
            assertThrows(DriverNotFoundException.class, () -> driverService.update(999_999L, driver("a","b")));
        }
    }

    @Nested
    class Delete {
        @Test
        void delete_WhenExisting_ShouldRemove() {
            Long id = existing.getId();
            driverService.delete(id);
            assertTrue(driverRepository.findById(id).isEmpty());
        }

        @Test
        void delete_WhenMissing_ShouldThrowDriverNotFoundException_AndLeaveStateUnchanged() {
            long before = driverRepository.count();
            assertThrows(DriverNotFoundException.class, () -> driverService.delete(999_999L));
            assertEquals(before, driverRepository.count());
        }

        @Test
        void delete_WhenNullId_ShouldThrowNullPointerException() {
            assertThrows(NullPointerException.class, () -> driverService.delete(null));
        }
    }
}
