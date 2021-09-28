.PHONY: default
default: | help

.PHONY: build-mvn
build-mvn: ## Build project and install to you local maven repo
	./mvnw clean install

.PHONY: run-local
run-local: ## Run the app locally (without docker)
	MICRONAUT_CONFIG_FILES=conf/application-local.yml java -Dcom.sun.management.jmxremote -jar  target/maskinporten-guardian-*.jar

.PHONY: release-dryrun
release-dryrun: ## Simulate a release in order to detect any issues
	./mvnw release:prepare release:perform -Darguments="-Dmaven.deploy.skip=true" -DdryRun=true

.PHONY: release
release: ## Release a new version. Update POMs and tag the new version in git
	./mvnw release:prepare release:perform -Darguments="-Dmaven.deploy.skip=true -Dmaven.javadoc.skip=true"

.PHONY: generate-test-virksomhetssertifikat
generate-test-virksomhetssertifikat: ## Generate a "virksomhetssertifikat" for test usage
	keytool -genkeypair \
		-alias testalias \
		-keypass pass123 \
		-storepass pass123 \
		-validity 9999 \
		-dname "CN=Unknown, OU=Unknown,O=SSB,L=Oslo,ST=Norway,C=NO" \
		-keyalg RSA \
		-keystore virksomhetssertifikat-test.p12

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
