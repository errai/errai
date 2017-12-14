Known problems
---

- Support CDI events of (un)annotated subtypes
- Support Stereotypes
- Un-ignore the following tests:
	- `StereotypesIntegrationTest`
	- `ClientEventSuperTypeTest`
	- `DynamicValidationIntegrationTest`
	- All async tests in `errai-ioc` and `errai-cdi`
- Fix support for `@Produces` annotation in `@ErraiApp`s targeting JAVA
- Double check if events observed only in server code are working properly with APT generators