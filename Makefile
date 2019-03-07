
docker-clean:
	@echo "Remove all non running containers"
	-docker rm `docker ps -q -f status=exited`
	@echo "Delete all untagged/dangling (<none>) images"
	-docker rmi `docker images -q -f dangling=true`


DOCKER_STOP=docker-compose --file docker-compose.yml down
## $(DOCKER_COMPOSE) down removing this because of multiple makefiles
dockerRun: ## Run MA in docker
	@echo starting container ##################%%%%%%%%%%%%%%%%%%%&&&&&&&&&&&&&&&&&&&&&&
	docker-compose --file docker-compose.yml up --force-recreate -d --build
	@echo started container ##################%%%%%%%%%%%%%%%%%%%&&&&&&&&&&&&&&&&&&&&&&

dockerStop:
	${DOCKER_STOP}

sleep:
	@echo Waiting for 5 minutes to read the metrics
	sleep 300
	@echo Wait finished