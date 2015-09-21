IHTSDO Refset and Translation Tool
==================================

This is a tool developed for IHTSDO to create, maintain, import, export, publish
and otherwise manage (extensional and intensional) refset creation. It additionally
supports a module for creating translations based on derived refsets with basic 
workflow.

This project hosts a basic UI that calls a set of REST APIs built around 
a SNOMED CT data model. The API is fully documented with Swagger (http://swagger.io)


A reference deployment of the system exists here:
https://refset.ihtsdotools.org/

Project Structure
-----------------

* top-level: aggregator for sub-modules (alphabetically):
  * admin: admin tools as maven plugins and poms
  * config: sample config files and data for windows dev environment and the reference deployment.
  * custom: project for demonstrating how to extend the platform
  * examples: sample code for learning how to use the API
  * integration-test: integration tests (JPA, REST, and mojo)
  * jpa-model: a JPA enabled implementation of "model"
  * jpa-services: a JPA enabled implementation of "services"
  * model: interfaces representing the RF2 domain model
  * parent: parent project for managing dependency versions.
  * rest: the REST service implementation
  * rest-client: a Java client for the REST services
  * services: interfaces representing the service APIs

Documentation
-------------
Find comprehensive documentation here: https://confluence.ihtsdotools.org/TBD

License
-------
See the included LICENSE.txt file.




  