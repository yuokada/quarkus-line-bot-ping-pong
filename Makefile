build:
ifeq ($(SCRATCH_BUILD), true)
	./mvnw clean
endif

ifeq ($(NATIVE_BUILD), true)
	./mvnw package -Pnative -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=docker
else
	./mvnw package
endif

deploy_jvm:
	cp ./sam.jvm.yaml ./samconfig.toml target/
	sam deploy --config-file samconfig.toml -t target/sam.jvm.yaml
