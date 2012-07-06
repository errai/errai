This project provides a JPA2 implementation that works in GWT. Entities are stored
and retrieved using browser-local storage.


== Attention Eclipse (m2e or m2eclipse) Users ==

The build configuration that Maven Integration for Eclipse creates from this
POM yields an Eclipse project that won't compile. Here's how you fix it:

In the Package Explorer view:

 - Expand src/main/java
 - Right click org.jboss.errai.jpa.api (within src/main/java)
 - From the popup menu, choose Build Path -> Use as Source Folder

The project should now compile, and dependent projects in your workspace
(errai-jpa-demo-grocery-list for example) should now also build cleanly.
