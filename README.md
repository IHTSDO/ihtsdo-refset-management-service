Main:
[![Build Status](https://jenkins.ihtsdotools.org/job/jobs/job/ihtsdo-refset-management-service/job/main/badge/icon)](https://jenkins.ihtsdotools.org/job/jobs/job/ihtsdo-refset-management-service/job/main/)

Develop:
[![Build Status](https://jenkins.ihtsdotools.org/job/jobs/job/ihtsdo-refset-management-service/job/develop/badge/icon)](https://jenkins.ihtsdotools.org/job/jobs/job/ihtsdo-refset-management-service/job/develop/)
[![Quality Gate Status](https://sonarqube.ihtsdotools.org/api/project_badges/measure?project=org.ihtsdo.otf.refset%3Arefset-aggregator&metric=alert_status&token=sqb_6e2cc291468d29b78402e2b4cef45680c64d6539)](https://sonarqube.ihtsdotools.org/dashboard?id=org.ihtsdo.otf.refset%3Arefset-aggregator)

![Contributers](https://img.shields.io/github/contributors/IHTSDO/ihtsdo-refset-management-service)
![Last Commit](https://img.shields.io/github/last-commit/ihtsdo/ihtsdo-refset-management-service)
![GitHub commit activity the past year](https://img.shields.io/github/commit-activity/m/ihtsdo/ihtsdo-refset-management-service)
&nbsp;&nbsp;
![Tag](https://img.shields.io/github/v/tag/IHTSDO/ihtsdo-refset-management-service)
![Java Version](https://img.shields.io/badge/Java_Version-17-green)
[![license](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE.md)
&nbsp;&nbsp;
![Lines of code](https://jenkins.ihtsdotools.org/job/jobs/job/ihtsdo-refset-management-service/job/develop/badge/icon?subject=Lines%20Of%20Code&status=${lineOfCode}&color=blue)
![Line Coverage](https://jenkins.ihtsdotools.org/job/jobs/job/ihtsdo-refset-management-service/job/develop/badge/icon?subject=Line%20Coverage&status=${lineCoverage}&color=${colorLineCoverage})
![Instruction Coverage](https://jenkins.ihtsdotools.org/job/jobs/job/ihtsdo-refset-management-service/job/develop/badge/icon?subject=Instruction%20Coverage&status=${instructionCoverage}&color=${colorInstructionCoverage})
![Branch Coverage](https://jenkins.ihtsdotools.org/job/jobs/job/ihtsdo-refset-management-service/job/develop/badge/icon?subject=Branch%20Coverage&status=${branchCoverage}&color=${colorBranchCoverage})

IHTSDO Refset and Translation Tool
==================================

This is a tool developed for IHTSDO to create, maintain, import, export, publish
and otherwise manage (extensional and intensional) refset creation. It additionally
supports a module for creating translations based on derived refsets with basic 
workflow.

This project hosts a basic UI that calls a set of REST APIs built around 
a SNOMED CT data model. The API is fully documented with Swagger (http://swagger.io)


A reference deployment of the system exists here:
https://uat-refset.ihtsdotools.org/

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
Find comprehensive documentation here: https://confluence.ihtsdotools.org/display/REFSET

License
-------
See the included LICENSE.txt file.




