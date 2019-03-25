
docker-clean:
	@echo "Remove all non running containers"
	-docker rm `docker ps -q -f status=exited`
	@echo "Delete all untagged/dangling (<none>) images"
	-docker rmi `docker images -q -f dangling=true`


DOCKER_STOP=docker-compose --file docker-compose.yml down

dockerRun: ## Run MA in docker
	@echo starting container ##################%%%%%%%%%%%%%%%%%%%&&&&&&&&&&&&&&&&&&&&&&
	docker-compose --file docker-compose.yml up --force-recreate -d --build controller
	sleep 600
	docker cp controller:/root/paworkspace/events-service/processor/conf/events-service-api-store.properties .
	EVENTS_SERVICE_HOST=$(cat events-service-api-store.properties | grep ad.accountmanager.key.controller|cut -d'=' -f2)
	export EVENTS_SERVICE_HOST=$(EVENTS_SERVICE_HOST)
	echo $EVENTS_SERVICE_HOST
	APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY='SJ5b2m7d1$$354' APPDYNAMICS_AGENT_ACCOUNT_NAME=$(APPDYNAMICS_AGENT_ACCOUNT_NAME) APPDYNAMICS_CONTROLLER_HOST_NAME=$(APPDYNAMICS_CONTROLLER_HOST_NAME) APPDYNAMICS_CONTROLLER_PORT=$(APPDYNAMICS_CONTROLLER_PORT) APPDYNAMICS_CONTROLLER_SSL_ENABLED=$(APPDYNAMICS_CONTROLLER_SSL_ENABLED) EVENTS_SERVICE_HOST=$(EVENTS_SERVICE_HOST) GLOBAL_ACCOUNT_NAME=$(GLOBAL_ACCOUNT_NAME) EVENTS_SERVICE_API_KEY=$(EVENTS_SERVICE_API_KEY) docker-compose --file docker-compose.yml up --force-recreate -d --build machine
	@echo started container ##################%%%%%%%%%%%%%%%%%%%&&&&&&&&&&&&&&&&&&&&&&

dockerStop:
	${DOCKER_STOP}

sleep:
	@echo Waiting for 5 minutes to read the metrics
	sleep 300
	@echo Wait finished