# Maskinporten Guardian Postman requests

A collection of postman requests that demonstrates how you...
* as a _service user_ can get hold of a keycloak access token
* as a _service user_ can get hold of a maskinporten access token
* as a _personal user_ can get hold of a maskinporten access token


## Getting started

1. Import the enclosed postman collection and environment variables ([how to](https://learning.postman.com/docs/getting-started/importing-and-exporting-data/))
2. Within Postman, configure the `Maskinporten Guardian` environment variables:

Variable                         | Example                                | Description
-------------------------------- | -------------------------------------- | --------------------------------------------
`environment`                    | `staging`                              | One of: `staging`, `prod`
`maskinporten_client_id`         | `65ebdbac-da2b-4545-9bbb-1e1994cb449c` | Maskinporten client ID as retrieved from "samhandlingsportalen"
`api_scopes`                     | `["some:scope", "some:other-scope"]`   | Maskinporten data scopes (json array)
`service_keycloak_client_secret` | `9900d5c9-24bb-417c-91a3-139d81e4ae7c` | Client secret for the maskinporten keycloak client ("Service account user"). Get this from #hjelp_bip.
`personal_keycloak_token`        | `eyJhbGciOiJSUz...JzIn0`               | Only needed if accessing maskinporten guardian as a personal user. See https://docs.dapla.ssb.no/dapla-developer/auth-tokens/#keycloak-personal-token for instructions on how to get hold of your token

Notice that you do not need to provide any keycloak client id - it is deduced from `{{maskinporten_client_id}}` (keycloak client id = `maskinporten-{{maskinporten_client_id}}`)


## Deduced environment variables

Notice that when running the postman requests, the following postman variables will be set in your environment: 

Variable                                     |  Description
-------------------------------------------- | -------------------------------------------------------------------------
`base64_encoded_keycloak_client_credentials` | You can ignore this. It is only used temporarily if retrieving a keycloak access token
`service_keycloak_token`                     | Keycloak access token that can be used to access Maskinporten Guardian
`maskinporten_token`                         | Maskinporten access token that can be used to access the API protected by Maskinporten


## Need help installing the Postman desktop app?

Mac:
```sh
brew install --cask postman
```
