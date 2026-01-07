SHELL := /bin/bash

.DEFAULT_GOAL := help

GRADLEW := ./gradlew
COMPOSE := docker compose
COMPOSE_FILE := compose.yml

SONAR_HOST_URL ?= http://localhost:9000
SONAR_LOGIN ?=

SONAR_ARGS :=
ifneq ($(strip $(SONAR_HOST_URL)),)
SONAR_ARGS += -Dsonar.host.url=$(SONAR_HOST_URL)
endif
ifneq ($(strip $(SONAR_LOGIN)),)
SONAR_ARGS += -Dsonar.login=$(SONAR_LOGIN)
endif

.PHONY: help \
	gradle \
	clean \
	build \
	run \
	test \
	test-course \
	test-student \
	test-class \
	jib \
	jib-docker \
	compose-up \
	compose-down \
	compose-ps \
	compose-logs \
	sonar-up \
	sonar-down \
	sonar-ps \
	sonar-logs \
	sonar-scan

help: ## Show available targets
	@awk 'BEGIN {FS=":.*##"; print "Usage: make <target>\n"} /^[a-zA-Z0-9_-]+:.*##/ {printf "  %-24s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

gradle: ## Run Gradle with TASKS="clean build"
	@if [ -z "$(TASKS)" ]; then echo "TASKS is required (example: make gradle TASKS=\"clean build\")"; exit 1; fi
	$(GRADLEW) $(TASKS)

clean: ## Gradle clean
	$(GRADLEW) clean

build: ## Gradle build
	$(GRADLEW) build

run: ## Run Spring Boot (bootRun)
	$(GRADLEW) bootRun

test: ## Run all tests
	$(GRADLEW) test

test-course: ## Run Course controller tests
	$(GRADLEW) courseControllerTest

test-student: ## Run Student controller tests
	$(GRADLEW) studentControllerTest

test-class: ## Run a specific test class (TEST='com.pkg.ClassTest')
	@if [ -z "$(TEST)" ]; then echo "TEST is required (example: make test-class TEST='com.abc.jibpilot.course.controller.CourseControllerIntTest')"; exit 1; fi
	$(GRADLEW) test --tests '$(TEST)'

jib: ## Build container image and push (Jib)
	$(GRADLEW) jib

jib-docker: ## Build local Docker image (Jib Docker)
	$(GRADLEW) jibDockerBuild

compose-up: ## Start production compose stack
	$(COMPOSE) -f $(COMPOSE_FILE) up -d

compose-down: ## Stop production compose stack
	$(COMPOSE) -f $(COMPOSE_FILE) down -v

compose-ps: ## List production compose services
	$(COMPOSE) -f $(COMPOSE_FILE) ps

compose-logs: ## Tail production compose logs
	$(COMPOSE) -f $(COMPOSE_FILE) logs -f

sonar-up: ## Start SonarQube service (docker compose)
	$(COMPOSE) -f $(COMPOSE_FILE) up -d sonarqube

sonar-down: ## Stop SonarQube service
	$(COMPOSE) -f $(COMPOSE_FILE) stop sonarqube

sonar-ps: ## Show SonarQube service status
	$(COMPOSE) -f $(COMPOSE_FILE) ps sonarqube

sonar-logs: ## Tail SonarQube logs
	$(COMPOSE) -f $(COMPOSE_FILE) logs -f sonarqube

sonar-scan: ## Run SonarQube scan (SONAR_LOGIN=token)
	$(GRADLEW) sonarqube $(SONAR_ARGS)
