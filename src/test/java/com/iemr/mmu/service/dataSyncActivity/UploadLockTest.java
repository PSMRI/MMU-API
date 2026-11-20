package com.iemr.mmu.service.dataSyncActivity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.mmu.repo.syncActivity_syncLayer.DataSyncGroupsRepo;

/**
 * Two uploads at the same time read the same unprocessed rows and central inserts each row twice (Kurupam van 415,
 * i_beneficiarydetails_rmnch, 24 Oct). Only one upload may run at a time.
 */
class UploadLockTest {

	private UploadDataToServerImpl upload;
	private DataSyncGroupsRepo groupsRepo;
	private ExecutorService executor;

	@BeforeEach
	void setUp() {
		upload = new UploadDataToServerImpl();
		groupsRepo = mock(DataSyncGroupsRepo.class);
		ReflectionTestUtils.setField(upload, "dataSyncGroupsRepo", groupsRepo);
		executor = Executors.newSingleThreadExecutor();
	}

	@AfterEach
	void tearDown() {
		executor.shutdownNow();
	}

	@Test
	void secondUploadIsRejectedWhileFirstIsRunning() throws Exception {
		CountDownLatch firstStarted = new CountDownLatch(1);
		CountDownLatch releaseFirst = new CountDownLatch(1);
		when(groupsRepo.findByDeleted(false)).thenAnswer(inv -> {
			firstStarted.countDown();
			releaseFirst.await(10, TimeUnit.SECONDS);
			return new ArrayList<>();
		});

		Future<String> first = executor.submit(() -> upload.getDataToSyncToServer(415, "user1", "key", null));
		assertTrue(firstStarted.await(10, TimeUnit.SECONDS));
		assertTrue(upload.isUploadInProgress());

		UploadInProgressException e = assertThrows(UploadInProgressException.class,
				() -> upload.getDataToSyncToServer(415, "user1", "key", null));
		assertEquals(UploadInProgressException.MESSAGE, e.getMessage());

		releaseFirst.countDown();
		assertEquals("No data to sync", first.get(10, TimeUnit.SECONDS));
		// the rejected request never reached the upload itself
		verify(groupsRepo, times(1)).findByDeleted(false);
	}

	@Test
	void lockIsReleasedAfterUploadFinishes() throws Exception {
		when(groupsRepo.findByDeleted(false)).thenReturn(new ArrayList<>());

		upload.getDataToSyncToServer(415, "user1", "key", null);
		assertFalse(upload.isUploadInProgress());
		upload.getDataToSyncToServer(415, "user1", "key", null);

		verify(groupsRepo, times(2)).findByDeleted(false);
	}

	@Test
	void lockIsReleasedAfterUploadFails() throws Exception {
		when(groupsRepo.findByDeleted(false)).thenThrow(new RuntimeException("central down"))
				.thenReturn(new ArrayList<>());

		assertThrows(RuntimeException.class, () -> upload.getDataToSyncToServer(415, "user1", "key", null));
		assertFalse(upload.isUploadInProgress());
		assertEquals("No data to sync", upload.getDataToSyncToServer(415, "user1", "key", null));
	}
}
