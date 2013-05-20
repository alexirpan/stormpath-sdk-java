package com.stormpath.sdk.impl.ds

import org.testng.annotations.Test
import com.stormpath.sdk.impl.http.*
import com.stormpath.sdk.impl.resource.*
import com.stormpath.sdk.resource.ResourceException
import com.stormpath.sdk.impl.ds.DefaultDataStore
import static org.testng.Assert.*
import static org.easymock.EasyMock.*

class DefaultDataStoreTest {

	@Test
	void testSave() {
		def reqExec = createStrictMock(RequestExecutor)
		def response = createStrictMock(Response)
		Map<String, Object> map = [ 'foo': 'bar']
		def marshaller = new JacksonMapMarshaller()
		def marshaledMap = marshaller.marshal(map)
		def bodyContent = new ByteArrayInputStream(marshaledMap.getBytes("UTF-8"))
		// First test error, then test non-error
		// If the response has no body, it causes a NPE in toMap()
		expect(response.hasBody()).andReturn true
		expect(response.getBody()).andReturn bodyContent
		expect(response.isError()).andReturn true
		
		bodyContent = new ByteArrayInputStream(marshaledMap.getBytes("UTF-8"))
		expect(response.hasBody()).andReturn true
		expect(response.getBody()).andReturn bodyContent
		expect(response.isError()).andReturn false
		replay response
		expect(reqExec.executeRequest(isA(Request))).andReturn response
		expect(reqExec.executeRequest(isA(Request))).andReturn response
		replay reqExec
		
		def dataStore = new DefaultDataStore(reqExec)
		def testResource = new TestSavableResource(dataStore)
		testResource.setProperty(AbstractResource.HREF_PROP_NAME, "testurl.com")
		try {
			dataStore.save(testResource)
			assertTrue false, "save did not raise an exception when given an reponse that was an error"
		} catch (ResourceException e) {
			// do nothing
		}
		
		testResource = new TestSavableResource(dataStore)
		testResource.setProperty(AbstractResource.HREF_PROP_NAME, "testurl.com")
		dataStore.save(testResource)
		assertEquals testResource.getProperty( 'foo' ), 'bar'
		verify response
		verify reqExec
	}
}