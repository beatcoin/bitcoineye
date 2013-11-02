package org.beatcoin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.beatcoin.pool.AddressPool;
import org.beatcoin.pool.NotInitializedException;
import org.beatcoin.pool.TestInitializer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PoolTester {
	private TestInitializer testInit;
	private AddressPool ap;
	private String a = "testf25e-baa7-452b-add7-e559d2cebe7a";

	@Before
	public void setUp() throws Exception {
		//simulate bitcoind here
		Map<String,Set<String>> data = new HashMap<>();
		Set<String> adr = new HashSet<>();
		adr.add("miapdrZdiUWmKYjDsbtxpPRHEPvKQaXBzb");
		adr.add("miapdrZdiUWmKYjDsbtxpPRHEPvKQaXBz2");
		adr.add("miapdrZdiUWmKYjDsbtxpPRHEPvKQaXBz3");
		data.put(a, adr);
		testInit = new TestInitializer(data);
		//create address pool with fake client
		ap = new AddressPool(testInit);
	}

	@Test
	public void testSync() throws NotInitializedException {
		String first = ap.reserveAddress(a);
		Assert.assertNotNull(first);
		String second = ap.reserveAddress(a);
		Assert.assertNotNull(second);
		String third = ap.reserveAddress(a);
		Assert.assertNotNull(third);
		String fourth = ap.reserveAddress(a);
		Assert.assertNull(fourth);
		boolean rel = ap.releaseAddress(a, third);
		Assert.assertTrue(rel);
		boolean rel2 = ap.releaseAddress(a, "imaginary");
		Assert.assertTrue(!rel2);
		String fifth = ap.reserveAddress(a);
		Assert.assertNotNull(fifth);
		Assert.assertEquals(third, fifth);
	}
	
	@Test
	public void testAdd() throws NotInitializedException {
		
	}

}
