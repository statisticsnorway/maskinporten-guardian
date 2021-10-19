import requests


def call_endpoint(api_endpoint, guardian_endpoint, keycloak_token, body):
    """
    Call an external API using the maskinporten guardian

    :param api_endpoint: URL to the target API
    :param guardian_endpoint: URL to the maskinporten guardian
    :param keycloak_token: the user's Keycloak token
    :param body: maskinporten request body, with the following structure:
    .. code-block:: json

    {
        "maskinportenClientId": "some-client-id",
        "scopes": [
            "ns:scope1",
            "ns:scope2"
        ]
    }
    :return: the endpoint json response
    """
    maskinporten_token = get_guardian_token(guardian_endpoint, keycloak_token, body)
    api_response = requests.get(api_endpoint,
        headers={
            'Authorization': 'Bearer %s' % maskinporten_token,
            'Accept': 'application/json'
        })
    if api_response.status_code == 200:
        return api_response.json()
    else:
        raise RuntimeError(f'Error calling target endpoint: <{api_response.status_code}: {api_response.text or api_response.reason}>')


def get_guardian_token(guardian_endpoint, keycloak_token, body):
    """
    Retrieve access token from makinporten guardian

    :param guardian_endpoint: URL to the maskinporten guardian
    :param keycloak_token: the user's Keycloak token
    :param body: maskinporten request body
    :return: the maskinporten access token
    """
    guardian_response = requests.post(guardian_endpoint,
        headers={
          'Authorization': 'Bearer %s' % keycloak_token,
          'Content-type': 'application/json'
        }, data=body)

    if guardian_response.status_code == 200:
        return guardian_response.json()['accessToken']
    else:
        raise RuntimeError(f'Error getting guardian token: <{guardian_response.status_code}: {guardian_response.text or guardian_response.reason}>')


if __name__ == '__main__':
    keycloak_token = '...'  # TODO: Get your personal token from Keycloak
    body = """
    {
      "maskinportenClientId": "...",
      "scopes": [
          "ns:scope1"
          ]
    }
    """
    guardian_endpoint = 'https://maskinporten-guardian-url/access-token'  # TODO: Set URL
    api_endpoint = 'https://target-api-url'  # TODO: Set URL

    api_response = call_endpoint(guardian_endpoint, api_endpoint, keycloak_token, body)
