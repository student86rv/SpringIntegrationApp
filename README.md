Spring Integration & Spring Batch lecture homework

1. Create delivery service messaging gateway that accepts packages.

There two options for delivering a package: delivery to store for further pickup or delivery to home. Also there are waiting for transfer packages that should be skipped.

class Package {
	...
	private DeliveryType deliveryType;
	…
}

enum DeliveryType {
	DTH, //delivery to home
	DTS, //delivery to store
	TRANSFER //waiting for transfer to another store
}

You need to create these two endpoints and integrate them with Spring Integration.

please use MessagingGateway annotation to create enpdoint.
useful code sample: 
https://github.com/spring-projects/spring-integration-samples/tree/master/dsl/cafe-dsl
