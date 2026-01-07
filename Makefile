SHELL := /bin/bash

.DEFAULT_GOAL := help

GRADLEW := ./gradlew
COMPOSE := docker compose
COMPOSE_FILE := compose.yml

.PHONY: help up down ps logs build test clean

help: ## Show available targets
	@awk 'BEGIN {FS=":.*##"; print "Usage: make <target>\n"} /^[a-zA-Z0-9_-]+:.*##/ {printf "  %-12s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

up: ## Start compose stack
	$(COMPOSE) -f $(COMPOSE_FILE) up -d

down: ## Stop compose stack
	$(COMPOSE) -f $(COMPOSE_FILE) down -v

ps: ## List compose services
	$(COMPOSE) -f $(COMPOSE_FILE) ps

logs: ## Tail compose logs
	$(COMPOSE) -f $(COMPOSE_FILE) logs -f

build: ## Gradle build
	$(GRADLEW) build

test: ## Run tests
	$(GRADLEW) test

clean: ## Gradle clean
	$(GRADLEW) clean
