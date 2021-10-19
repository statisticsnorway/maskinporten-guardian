# Maskinporten Guardian

<img align="right" width="200" src="doc/img/bridge-keeper-558x336.png">

Maskinporten Guardian allows SSB services and trusted users to retrieve
[Maskinporten](https://samarbeid.digdir.no/maskinporten/dette-er-maskinporten/96) access tokens.

> TLDR: Maskinporten Guardian exchanges keycloak access tokens for Maskinporten access tokens.

The Guardian manages sensitive information such as certificates and secrets. It keeps track of who can retrieve access
tokens on behalf of different _Maskinporten clients_. The Guardian also handles audit logging, making it easy to see
_who_ requested access to _what_ API _when_.

## What's Maskinporten?

Maskinporten is a national, common solution providing secure authentication and access control that enables data
exchange between different Norwegian data providers. A [growing number](https://samarbeid.digdir.no/maskinporten/maskinporten/995)
of APIs are being protected by Maskinporten.

Access to Maskinporten is administered via _Samhandlingsportalen_. Organizations (e.g., SSB) that need to consume an
API protected by Maskinporten will create a _maskinporten client_ in this administrative portal. The _maskinporten
clients_ are configured to be trusted with a set of possible "data scopes" for which the client can retrieve data. It
is a prerequisite that an agreement between the data-consuming organization (e.g., SSB) and the data-providing
organizations (e.g., Skatteetaten) must be established in advance.


## How can I get access?

> ~~None shall pass!~~ Only trustworthy ones shall pass!

<img align="left" src="doc/img/guardian-120x204.png">

So, you want to become a trusted party? Here are the requirements:

1. Get hold of a maskinporten client - and a set of scopes for the API that you want to consume.

TODO

![Overview](doc/img/maskinporten-guardian-overview.png)


## Development

Use `make` for common tasks...
```
build-mvn                           Build project and install to you local maven repo
run-local                           Run the app locally (without docker)
release-dryrun                      Simulate a release in order to detect any issues
release                             Release a new version. Update POMs and tag the new version in git
generate-test-virksomhetssertifikat Generate a "virksomhetssertifikat" for test
```


## References

* https://docs.digdir.no/maskinporten_overordnet.html
* https://github.com/ks-no/fiks-maskinporten


## Micronaut Documentation

- [User Guide](https://docs.micronaut.io/latest/guide/index.html)
- [API Reference](https://docs.micronaut.io/latest/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/latest/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
---
