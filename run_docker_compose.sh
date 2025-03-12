docker-compose \
-f docker-compose.yml \
-f docker-compose.mongo.yml \
-f docker-compose.rabbitmq.yml \
-f docker-compose.manager.yml \
-f docker-compose.worker.yml \
up