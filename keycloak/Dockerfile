FROM quay.io/keycloak/keycloak:23.0.1 as builder

# Enable health and metrics support
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
ENV KC_HTTP_ENABLED=true

# Configure a database vendor
ENV KC_DB=postgres
#create folder
WORKDIR /opt/keycloak
# copy source to directory
ADD recolnat-oauth2-realm.json /opt/keycloak


# for demonstration purposes only, please make sure to use proper certificates in production instead
RUN keytool -genkeypair -storepass password -storetype PKCS12 -keyalg RSA -keysize 2048 -dname "CN=server" -alias server -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -keystore conf/server.keystore
#performs a set of optimizations for the startup and runtime
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:23.0.1
COPY --from=builder /opt/keycloak/ /opt/keycloak/

RUN /opt/keycloak/bin/kc.sh import --file /opt/keycloak/recolnat-oauth2-realm.json

RUN /opt/keycloak/bin/kc.sh  show-config

# change these values to point to a running postgres instance
#la base du vendor
##ENV KC_DB=postgres
#URl  jdbc:postgresql://host:port/database  ( host= hostname du server , defaut localhost)
##ENV KC_DB_URL=jdbc:postgresql://postgres:5432/keycloak
##ENV KC_DB_USERNAME=admin
##ENV KC_DB_PASSWORD=admin
#ENV KC_HOSTNAME=localhost
# ajout d un entrypoint ( invariant) et sous forme de tableau (si sous forme de chaine : ignore le CMD)
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
# ajout d une commande (en complement de ENTRYPOINT)qui peut etre override dans la commande docker
#CMD "start-dev"