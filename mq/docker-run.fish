#/usr/bin/env fish

docker run --rm -d --name my_rabbitmq -p 15672:15672 -p 5672:5672 rabbitmq:3-management
