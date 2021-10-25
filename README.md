# Test Assessment
## A webservice that handles user authentication
### Workflow
The web service works based on tokens that are given out to each user. Every token has an assigned lifetime,
with the user being able to revoke their token at any time. The user is free to check if their token is valid
at any time, and they are able to renew their token by sending a request for a new one.
### Routes
The application exposes 5 routes:
* `/users` with two methods, `GET` and `POST`
    * `GET` requires a valid token in the `Authorization` header and will return a list of all users
    * `POST` requires a valid `User` in the body and will create the `User`
* `/users/{id}` with one method, `PUT`, which will update the `User` by its id. This endpoint requires a valid `User`
in the body and a valid token in the `Authorization` header
* `/users/{id}/token` with one method, `POST`, which will request a new token for the `User`. This endpoint requires
the `password` of the `User` to be sent in the request body.
* `/users/{id}/validate-token` with one method, `POST`, which will either send a body confirming that the token for the
`User` is still valid or return HTTP 401 if the token is no longer valid. This endpoint requires a token to be sent in
the `Authorization` header.
* `/users/{id}/revoke-token` with one method, `POST`, which will invalidate the token that was sent through the
`Authorization` header if itwas valid, or send a HTTP 401 if the token is invalid. 