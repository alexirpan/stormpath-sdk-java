package com.stormpath.sdk.impl.ds

import com.stormpath.sdk.impl.resource.AbstractInstanceResource

/**
 * @since 0.4.1
 */
class TestSavableResource extends AbstractInstanceResource {

	// ResourceFactory doesn't see these methods unless they're public? Messy fix
    public TestSavableResource(InternalDataStore dataStore) {
        super(dataStore)
    }

    public TestSavableResource(InternalDataStore dataStore, Map<String, Object> properties) {
        super(dataStore, properties)
    }
	
	public void setProperty(String name, Object value) {
		super.setProperty(name, value)
	}
}