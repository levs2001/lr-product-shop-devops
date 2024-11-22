# https://skillbox.ru/media/base/kak_rabotat_s_docker_upakovka_spring_boot_prilozheniya_v_konteyner/
FROM eclipse-temurin:21.0.2_13-jdk
ARG JAR_FILE=/target/lr-product-shop-1.0.0.jar
WORKDIR /opt/app
COPY ${JAR_FILE} lr-product-shop-app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "lr-product-shop-app.jar"]