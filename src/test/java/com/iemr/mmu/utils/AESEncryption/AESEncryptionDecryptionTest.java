/*
* AMRIT – Accessible Medical Records via Integrated Technology 
* Integrated EHR (Electronic Health Records) Solution 
*
* Copyright (C) "Piramal Swasthya Management and Research Institute" 
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.mmu.utils.AESEncryption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AESEncryptionDecryptionTest {

	@Test
	@DisplayName("an encrypted value decrypts back to the original")
	void encryptThenDecrypt_returnsTheOriginalValue() throws Exception {
		AESEncryptionDecryption cipher = new AESEncryptionDecryption();

		String encrypted = cipher.encrypt("/mmu/reports/2024/report.pdf");

		assertNotEquals("/mmu/reports/2024/report.pdf", encrypted);
		assertEquals("/mmu/reports/2024/report.pdf", cipher.decrypt(encrypted));
	}

	@Test
	@DisplayName("each encryption uses a fresh initialisation vector")
	void encrypt_usesAFreshInitialisationVectorEachTime() throws Exception {
		AESEncryptionDecryption cipher = new AESEncryptionDecryption();

		assertNotEquals(cipher.encrypt("same value"), cipher.encrypt("same value"));
	}

	@Test
	@DisplayName("an explicitly set key is used for both directions")
	void setKey_isUsedForBothDirections() throws Exception {
		AESEncryptionDecryption cipher = new AESEncryptionDecryption();
		cipher.setKey("a-custom-key");

		assertEquals("value", cipher.decrypt(cipher.encrypt("value")));
	}

	@Test
	@DisplayName("a value that was not produced by this cipher cannot be decrypted")
	void decrypt_failsForAValueThisCipherDidNotProduce() {
		AESEncryptionDecryption cipher = new AESEncryptionDecryption();

		assertThrows(Exception.class, () -> cipher.decrypt("bm90LWVuY3J5cHRlZC1hdC1hbGw="));
	}
}
