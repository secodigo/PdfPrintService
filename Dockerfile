# Stage 1: Build com GraalVM Native Image
FROM container-registry.oracle.com/graalvm/native-image:21 AS builder

WORKDIR /app
COPY . /app

# Configuração para compilação native
ENV MAVEN_OPTS="-Xmx4g"

# Instalar o Maven e compilar para native image
RUN ./mvnw clean package native:compile -Pnative -DskipTests

# Stage 2: Imagem mínima para execução
FROM oraclelinux:9-slim

WORKDIR /app

# Copiar apenas o executável nativo
COPY --from=builder /app/target/print-service .

# Diretório para recursos estáticos (como imagens de logo)
RUN mkdir -p /app/resources/images

# Definir variáveis de ambiente com valores padrão
ENV SERVER_PORT=8089

# Expor a porta configurável
EXPOSE ${SERVER_PORT}

# Comando de execução com passagem de parâmetros
ENTRYPOINT ["./print-service", "--server.port=${SERVER_PORT}"]
