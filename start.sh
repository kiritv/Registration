#!/bin/sh

# cat << EOF > ./application.properties
# server.servlet.context-path=/api/
# server.port=$SERVER_PORT
# spring.datasource.hikari.connectionTimeout=20000
# spring.datasource.hikari.maximumPoolSize=5
# spring.datasource.hikari.data-source-properties.stringtype=unspecified
# spring.datasource.url=$DB_ADDRESS
# spring.datasource.username=$DB_USER
# spring.datasource.password=$DB_PASSWORD
# registration.integration.ims.api.get.url=$IMS_API_GET
# registration.integration.ims.api.post.url=$IMS_API_POST
# management.endpoints.enabled-by-default=true
# management.endpoints.jmx.exposure.include=health,env,trace,loggers,metrics,auditevents,heapdump,configprops
# management.endpoints.web.exposure.include=health,env,trace,loggers,metrics,auditevents,heapdump,configprops
# management.endpoint.health.enabled=true
# management.endpoint.env.enabled=true
# management.endpoint.loggers.enabled=true
# management.endpoint.metrics.enabled=true
# management.endpoint.auditevents.enabled=true
# management.endpoint.heapdump.enabled=true
# management.endpoint.configprops.enabled=true
# management.endpoint.health.status.order=fatal,down,out-of-service,unknown,up
# management.endpoint.beans.cache.time-to-live=10s
# management.endpoints.web.cors.allowed-methods=GET,POST
# management.endpoint.health.group.custom.show-details=always
# spring.servlet.multipart.enabled=true
# spring.servlet.multipart.file-size-threshold=2KB
# spring.servlet.multipart.max-file-size=128MB
# spring.servlet.multipart.max-request-size=128MB
# server.max-http-header-size=65536
# server.max-http-post-size=65536
# logging.level.org.springframework=INFO
# logging.level.com.metrostarsystems=INFO
# logging.level.com.zaxxer=DEBUG
# logging.level.root=ERROR
# EOF
env
java -jar /app/*.jar
