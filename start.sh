#!/usr/bin/env bash
if [ -z "$1" ]
  then
    echo "$(tput setaf 1)You need input the full path to the jar file$(tput setaf 0)"
    exit 0
fi
docker volume create pgsql_foodcountry_data || echo "$(tput setaf 1)creating db volume end with error$(tput setaf 0)" &&
docker run -m 512m --name foodcountry_db -e POSTGRES_PASSWORD=foodcountry -e POSTGRES_USER=foodcountry -e POSTGRES_DB=foodcountry -v pgsql_foodcountry_data:/var/lib/postgresql/data --restart unless-stopped -p 5432:5432/tcp -d postgres:11.3-alpine || echo "$(tput setaf 1)creating db container end with error$(tput setaf 0)" &&
docker run -m 512m --restart unless-stopped --name foodcountry_ktor -v $1:/app/app.jar --link foodcountry_db:db -p 0.0.0.0:8080:8080/tcp -d -w '/app' openjdk:11.0.3-jre-slim-stretch bash -c 'java -jar app.jar' || echo "$(tput setaf 1)creating app container end with error$(tput setaf 0)"